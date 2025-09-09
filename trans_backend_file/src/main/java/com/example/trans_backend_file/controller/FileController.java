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
import com.example.trans_backend_file.model.entity.Project;
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
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class FileController {

    @Resource
    private FileService fileService;
    @Resource
    private ApplicationContext applicationContext;
    //todo 文件上传的幂等性保证
    @PostMapping("/upload")
    public BaseResponse<Long> uploadFile(@RequestPart(value = "file") MultipartFile file, @RequestParam(value = "projectId") Long projectId,@RequestParam(value = "groupId") Long groupId) {
        ThrowUtils.throwIf(file==null,ErrorCode.PARAMS_ERROR);
        Long fileId = fileService.upload(file, projectId,groupId);
        ThrowUtils.throwIf(fileId == null, ErrorCode.SYSTEM_ERROR, "上传失败");
        return ResultUtils.success(fileId);
    }


    @GetMapping("/selectFiles")
    public BaseResponse<List<File>> selectAllFiles(Long projectId){
        List<File> files = fileService.selectAll(projectId);
        return ResultUtils.success(files);
    }
    @PostMapping("/deleteFile")
    public BaseResponse<?>  deleteFile(@RequestBody  List<Long> list){
        boolean b = fileService.deleteFiles(list);
        if(b){
            return ResultUtils.success(b);
        }else{
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
    }

    @PostMapping("/renameFile")
    public BaseResponse<?> renameFile(Long fileId, String newName) {
        ThrowUtils.throwIf(fileId == null || newName == null || newName.isEmpty(), ErrorCode.PARAMS_ERROR, "fileId或newName不能为空");
        fileService.renameFile(fileId, newName);
        return ResultUtils.success(null);
    }


    @PostMapping("/export")
    public void export(Long fileId, HttpServletResponse response){
        ThrowUtils.throwIf(fileId == null, ErrorCode.PARAMS_ERROR, "fileId不能为空");
        try {
            fileService.export(fileId, response);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出失败");
        }
    }



}
