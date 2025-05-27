package com.example.trans_backend_file.controller;

import com.example.trans_backend_common.aop.AuthCheck;
import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.common.ResultUtils;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.GlobalExceptionHandler;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.model.dto.SelectTransTextRequest;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.util.MinioUtil;
import com.oracle.webservices.internal.api.message.PropertySet;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class FileController {

    @Resource
    private FileService fileService;
    @Resource
    private ApplicationContext applicationContext;

    @PostMapping("/upload")
    public BaseResponse<Long> uploadFile(@RequestPart(value = "file") MultipartFile file, @RequestParam(value = "projectId") Long projectId) {
        ThrowUtils.throwIf(file==null,ErrorCode.PARAMS_ERROR);
        Long fileId = fileService.upload(file, projectId);
        ThrowUtils.throwIf(fileId == null, ErrorCode.SYSTEM_ERROR, "上传失败");
        return ResultUtils.success(fileId);
    }
    @GetMapping("/selectFiles")
    public BaseResponse<List<File>> selectAllFiles(int projectId){
        List<File> files = fileService.selectAll(projectId);
        return ResultUtils.success(files);
    }
    @PostMapping("/deleteFile")
    public BaseResponse<?>  deleteFile(@RequestBody  List<Integer> list){
        boolean b = fileService.deleteFiles(list);
        if(b){
            return ResultUtils.success(b);
        }else{
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
        }
    }

    @RequestMapping("/test")
    public String test(){
        Map<String, GlobalExceptionHandler> beansOfType = applicationContext.getBeansOfType(GlobalExceptionHandler.class);
        String[] beanNamesForType = applicationContext.getBeanNamesForType(GlobalExceptionHandler.class);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"测试"
        );
    }

    @PostMapping("/export")
    public void export(Long fileId, HttpServletResponse response){


    }



}
