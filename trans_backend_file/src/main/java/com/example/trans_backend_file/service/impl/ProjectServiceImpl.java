package com.example.trans_backend_file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_file.model.entity.Project;
import com.example.trans_backend_file.mapper.ProjectMapper;
import com.example.trans_backend_file.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
* @author 刘焱刚
* @description 针对表【project】的数据库操作Service实现
* @createDate 2025-04-29 19:42:14
*/
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {



    @Override
    public List<Project> selectListById(Long userId) {
        QueryWrapper<Project> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<Project> projects = baseMapper.selectList(queryWrapper);
        return projects;
    }

    @Override
    public Project create(Long userId, String name) {
        Project project = new Project();
        project.setUserId(userId);
        project.setName(name);
        boolean save = save(project);
        if(save){
            return project;
        }else{
            return null;
        }
    }

    @Override
    public boolean update(Long projectId, String name) {
        Project project = getById(projectId);//先查询是否存在
        if(project==null||!Objects.equals(project.getUserId(), BaseContext.getUser().getId())){
            return false;
        }
        project.setName(name);
        return saveOrUpdate(project);
    }

    @Override
    public boolean delete(Long projectId) {
        Project project = getById(projectId);//先查询是否存在
        if(project==null||!Objects.equals(project.getUserId(), BaseContext.getUser().getId())){
            return false;
        }
        //todo 删除项目下的所有文件和翻译对
        return removeById(projectId);
    }

}




