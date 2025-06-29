package com.example.trans_backend_file.service;

import com.example.trans_backend_file.model.entity.Project;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 刘焱刚
* @description 针对表【project】的数据库操作Service
* @createDate 2025-04-29 19:42:14
*/
public interface ProjectService extends IService<Project> {
    List<Project> selectListById(Long userId);
    Project create(Long userId,String name,Long groupId);
    boolean update(Long projectId,String name);
    boolean delete(Long projectId);

    List<Project> getProjectsByGroupId(Long groupId);

}
