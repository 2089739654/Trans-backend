package com.example.trans_backend_file.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.common.ResultUtils;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.TransBackendFileApplication;
import com.example.trans_backend_file.config.MqConfig;
import com.example.trans_backend_file.enums.LanguageType;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.model.entity.FileProjectRecord;
import com.example.trans_backend_file.model.entity.SysRecord;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.FileProjectRecordService;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.mapper.FileMapper;
import com.example.trans_backend_file.service.SysRecordService;
import com.example.trans_backend_file.service.TranslationPairsService;
import com.example.trans_backend_file.util.MinioUtil;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 20897
 * @description 针对表【file】的数据库操作Service实现
 * @createDate 2025-04-18 09:42:36
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
        implements FileService {
    @Resource
    MinioUtil minioUtil;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${mq.exchange:TransExchange}")
    private String EXCHANGE;

    @Value("${mq.routingKey:resolve}")
    private String ROUTING_KEY;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SysRecordService sysRecordService;
    @Autowired
    private TransBackendFileApplication transBackendFileApplication;

    @Resource
    private FileProjectRecordService fileProjectRecordService;

    @Resource
    private TranslationPairsService translationPairsService;


    //可做两阶段提交 补偿事务等 todo
    @Override
    public Long upload(MultipartFile file, Long projectId) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        String path = null;
        //解析文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
        User user = BaseContext.getUser();
        try {
            path = MinioUtil.uploadFile(file.getInputStream(), String.valueOf(user.getId()), originalFilename);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
        // 存储文件元信息  可加消息队列
        File targetFile = new File();
        targetFile.setFileExtension(fileExtension);
        targetFile.setFilePath(path);
        targetFile.setFileName(file.getOriginalFilename());
        targetFile.setUserId(user.getId());
        targetFile.setFileSize(file.getSize());
        targetFile.setLngType(LanguageType.ENGLISH.getCode());
        targetFile.setProjectId(projectId);
        //生成sysRecord
        SysRecord sysRecord = new SysRecord();
        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicInteger retry = new AtomicInteger();
        //生成file-project-record
        FileProjectRecord fileProjectRecord=new FileProjectRecord();
        int u=1;
        while (retry.get() < 3 && !flag.get()) {

            boolean res= Boolean.TRUE.equals(transactionTemplate.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus status) {
                    try {
                        int insert = baseMapper.insert(targetFile);
                        if (insert != 1) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件信息存储失败");
                        sysRecord.setContent(JSONUtil.toJsonStr(targetFile));
                        int add = sysRecordService.add(sysRecord);
                        if (add != 1) throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件解析记录存储失败");
                        fileProjectRecord.setFileId(targetFile.getId());
                        fileProjectRecord.setProjectId(projectId);
                        boolean save = fileProjectRecordService.save(fileProjectRecord);
                        ThrowUtils.throwIf(!save,ErrorCode.PARAMS_ERROR,"file-project记录存储失败");
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        status.setRollbackOnly();
                        retry.getAndIncrement();
                        return false;
                    }
                    return true;
                }
            }));
            flag.set(res);
        }
        if (retry.get() == 3) {
            //发送消息队列 todo
            return null;
        }
        //发送解析任务
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY,
                    MqConfig.getMessage(targetFile, String.valueOf(sysRecord.getId())), MqConfig.getCorrelationData());
        } catch (AmqpException e) {
            log.error("消息发送失败,信息id:"+sysRecord.getId());
        }
        return targetFile.getId();
    }

    @Override
    public void export(Long fileId, HttpServletResponse response) {
        QueryWrapper<File> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", fileId);
        File file = baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不存在");

        List<TranslationPairs> list= translationPairsService.selectAllOrderByPosition(fileId);
        int end=list.get(list.size()-1).getPosition();
        String res=null;

        try (InputStream inputStream = MinioUtil.getFileStream(file)) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            boolean flag=false;
            while ((line = bufferedReader.readLine()) != null) {
                if(flag){
                    if(line.charAt(0)>='a'&&line.charAt(0)<='z'){
                        stringBuilder.append(" ").append(line);
                    }else {
                        stringBuilder.append("\n").append(line);
                    }
                }else {
                    stringBuilder.append(line);
                    flag=true;
                }
            }
            res=stringBuilder.toString();
            for (int i = 1; i <= end; i++) {
                res=res.replace(list.get(i).getSourceText(),list.get(i).getTranslatedText());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        response.reset();
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment; filename=" + file.getFileName());
        ServletOutputStream outputStream=null;
        try {
            outputStream = response.getOutputStream();
            outputStream.write(res.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                if (outputStream!=null)outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }


    @Override
    public List<File> selectAll(int ProjectId) {
        return listByIds(Collections.singletonList(ProjectId));
    }

    @Override
    public boolean deleteFiles(List<Integer> ids) {
        List<File> files = listByIds(ids);
        try {
            for (File file : files) MinioUtil.removeFile(file.getFilePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return removeByIds(ids);
    }


}




