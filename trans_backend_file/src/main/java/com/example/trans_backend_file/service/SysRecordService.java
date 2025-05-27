package com.example.trans_backend_file.service;

import com.example.trans_backend_file.model.entity.SysRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 20897
* @description 针对表【sys_record】的数据库操作Service
* @createDate 2025-04-29 21:05:14
*/
public interface SysRecordService extends IService<SysRecord> {

    int add(SysRecord sysRecord);

    void error(Long id);

    void success(Long id);

    Integer select(Long id);

    List<SysRecord> getAll();

}
