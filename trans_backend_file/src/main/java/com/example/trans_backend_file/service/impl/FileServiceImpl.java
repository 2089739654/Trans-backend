package com.example.trans_backend_file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.mapper.FileMapper;
import com.example.trans_backend_file.util.MinioUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

/**
* @author 20897
* @description 针对表【file】的数据库操作Service实现
* @createDate 2025-04-18 09:42:36
*/
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
    implements FileService {




    @Override
    public String upload(MultipartFile file) {
        ThrowUtils.throwIf(file.isEmpty(),ErrorCode.PARAMS_ERROR,"文件不能为空");
        String path=null;
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        User user = BaseContext.getUser();
        try {
            path= MinioUtil.uploadFile(file.getInputStream(), String.valueOf(user.getId()), originalFilename);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }
        // 存储文件元信息  可加消息队列
        int retry=0;
        File file1 = new File();
        file1.setFileExtension(fileExtension);
        file1.setFilePath(path);
        file1.setFileName(file.getOriginalFilename());
        file1.setUserId(user.getId());
        file1.setFileSize(file.getSize());
        while (retry<3){
            try{
                int insert = baseMapper.insert(file1);
                if(insert>0)break;
            }catch (Exception e){
                retry++;
                break;
            }
            retry++;
        }
        if(retry==3){
            //发送消息队列 todo
        }
        //发送解析任务
        return path;
    }

}




