package com.example.trans_backend_file.controller;

import com.example.trans_backend_common.aop.AuthCheck;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.GlobalExceptionHandler;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.util.MinioUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@RestController
public class FileController {

    @Resource
    private FileService fileService;
    @Resource
    private ApplicationContext applicationContext;

    @PostMapping("/upload")
    public String uploadFile(@RequestPart(value = "file") MultipartFile file){
        ThrowUtils.throwIf(file==null,ErrorCode.PARAMS_ERROR);
        return fileService.upload(file);
    }

    @RequestMapping("/test")
    public String test(){
        Map<String, GlobalExceptionHandler> beansOfType = applicationContext.getBeansOfType(GlobalExceptionHandler.class);
        String[] beanNamesForType = applicationContext.getBeanNamesForType(GlobalExceptionHandler.class);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"测试"
        );
    }




}
