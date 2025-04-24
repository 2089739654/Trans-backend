package com.example.trans_backend_file.service;

import com.example.trans_backend_file.model.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author 20897
* @description 针对表【file】的数据库操作Service
* @createDate 2025-04-18 09:42:36
*/
public interface FileService extends IService<File> {

    String upload(MultipartFile file);


}
