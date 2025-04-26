package com.example.trans_backend_file.manager;

import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.util.MinioUtil;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class ResolveManager {


    @Value("${minio.bucketName}")
    private static String bucketName;

    @Resource
    private TranslationManager translationManager;

    public  void resolve(File file){
//        String fileExtension = file.getFileExtension();
        String filePath = file.getFilePath();
        int index = filePath.indexOf(bucketName) + bucketName.length();
        String substring = filePath.substring(index+1);
        InputStream inputStream = MinioUtil.get(substring);
        List<String> result=new ArrayList<>();
        try {
            result = doResolve(inputStream);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析文件失败");
        }
//        将解析结果批量翻译
        translationManager.translate(result);
    }

    protected abstract List<String> doResolve(InputStream inputStream);


}
