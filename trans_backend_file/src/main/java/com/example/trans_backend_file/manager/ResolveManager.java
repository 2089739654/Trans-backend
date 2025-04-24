package com.example.trans_backend_file.manager;

import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.util.MinioUtil;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;

public abstract class ResolveManager {


    @Value("${minio.bucketName}")
    private static String bucketName;

    public  void resolve(File file){
//        String fileExtension = file.getFileExtension();
        String filePath = file.getFilePath();
        int index = filePath.indexOf(bucketName) + bucketName.length();
        String substring = filePath.substring(index+1);
        InputStream inputStream = MinioUtil.get(substring);
        doResolve(inputStream);

    }

    protected abstract void doResolve(InputStream inputStream);


}
