package com.example.trans_backend_file.manager.resolve.impl;

import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.manager.TextVectorizationManager;
import com.example.trans_backend_file.manager.TranslationManager;
import com.example.trans_backend_file.manager.resolve.ResolveService;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.model.entity.Project;
import com.example.trans_backend_file.model.entity.TeamTransPairs;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.ProjectService;
import com.example.trans_backend_file.service.TeamTransPairsService;
import com.example.trans_backend_file.service.TranslationPairsService;
import com.example.trans_backend_file.util.MinioUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class ResolveManager implements ResolveService {


    @Value("${minio.bucketName:null}")
    private String bucketName;

    @Resource
    private TextVectorizationManager textVectorizationManager;

    @Resource
    private TranslationPairsService translationPairsService;

    @Resource
    private TeamTransPairsService teamTransPairsService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public final void resolve(File file){
        //获取文件流
        InputStream inputStream = MinioUtil.getFileStream(file);
        List<String> result=null;
        //解析文件
        try {
            result = doResolve(inputStream);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析文件失败");
        }
        //存储元数据
        int len=result.size();
        if(file.getGroupId()==null){
            List<TranslationPairs> list=new ArrayList<>();
            for (int i=0;i<len;i++){
                TranslationPairs translationPairs=new TranslationPairs();
                translationPairs.setSourceText(result.get(i));
                translationPairs.setFileId(file.getId());
                translationPairs.setPosition(i+1);
                list.add(translationPairs);
            }
            boolean res = translationPairsService.saveBatch(list, list.size());
            ThrowUtils.throwIf(!res, ErrorCode.SYSTEM_ERROR, "存储元数据失败");
        }else {
            //如果是团队项目，则存储到团队翻译对中
            List<TeamTransPairs> list=new ArrayList<>();
            for (int i = 0; i < len; i++) {
                TeamTransPairs teamTransPairs = new TeamTransPairs();
                teamTransPairs.setSourceText(result.get(i));
                teamTransPairs.setFileId(file.getId());
                teamTransPairs.setPosition(i + 1);
                teamTransPairs.setEditorId(file.getUserId());
                teamTransPairs.setTimestamp(0L);
                list.add(teamTransPairs);
            }
            boolean res = teamTransPairsService.saveBatch(list, list.size());
            ThrowUtils.throwIf(!res, ErrorCode.SYSTEM_ERROR, "存储元数据失败");
            //存redis 构建缓存
            String key= "file:" + file.getId();
            stringRedisTemplate.opsForHash().putAll(key,list.stream().collect(Collectors.toMap(TeamTransPairs::getPosition, JSONUtil::toJsonStr)));
            stringRedisTemplate.expire(key, 3, TimeUnit.DAYS); // 设置过期时间为3天
        }

    }

    protected abstract List<String> doResolve(InputStream inputStream);


}
