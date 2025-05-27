package com.example.trans_backend_file.manager.resolve.impl;

import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.manager.TextVectorizationManager;
import com.example.trans_backend_file.manager.TranslationManager;
import com.example.trans_backend_file.manager.resolve.ResolveService;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.TranslationPairsService;
import com.example.trans_backend_file.util.MinioUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class ResolveManager implements ResolveService {


    @Value("${minio.bucketName:null}")
    private String bucketName;

    @Resource
    private TextVectorizationManager textVectorizationManager;

    @Resource
    private TranslationPairsService translationPairsService;



    public final void resolve(File file){
        //获取文件流
        InputStream inputStream = MinioUtil.getFileStream(file);
        List<String> result=new ArrayList<>();
        //解析文件
        try {
            result = doResolve(inputStream);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析文件失败");
        }
        //存储元数据
        List<TranslationPairs> list=new ArrayList<>();
        int len=result.size();
        for (int i=0;i<len;i++){
            TranslationPairs translationPairs=new TranslationPairs();
//            Float[] vectorization = textVectorizationManager.vectorization(result.get(i));
//            translationPairs.setVector(vectorization);
            translationPairs.setSourceText(result.get(i));
            translationPairs.setFileId(file.getId());
            translationPairs.setPosition(i+1);
            list.add(translationPairs);
        }
        boolean res = translationPairsService.saveBatch(list, list.size());
        ThrowUtils.throwIf(!res, ErrorCode.SYSTEM_ERROR, "存储元数据失败");
    }

    protected abstract List<String> doResolve(InputStream inputStream);


}
