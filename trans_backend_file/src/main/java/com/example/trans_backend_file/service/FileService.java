package com.example.trans_backend_file.service;

import com.example.trans_backend_file.model.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* @author 20897
* @description 针对表【file】的数据库操作Service
* @createDate 2025-04-18 09:42:36
*/
public interface FileService extends IService<File> {

    Long upload(MultipartFile file, Long projectId);


    void export(Long fileId, HttpServletResponse response);



    List<File> selectAll(Long ProjectId);

    boolean deleteFiles(List<Long> ids);

    void renameFile(Long fileId, String newName);

}
