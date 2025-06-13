package com.example.trans_backend_file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_file.model.entity.FileProjectRecord;
import com.example.trans_backend_file.model.entity.Project;
import com.example.trans_backend_file.mapper.ProjectMapper;
import com.example.trans_backend_file.model.entity.TranslationPairs;
import com.example.trans_backend_file.service.FileProjectRecordService;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.service.ProjectService;
import com.example.trans_backend_file.service.TranslationPairsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author 刘焱刚
* @description 针对表【project】的数据库操作Service实现
* @createDate 2025-04-29 19:42:14
*/
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {


    @Resource
    private TranslationPairsService translationPairsService;
    @Resource
    private FileService fileService;

    @Resource
    private FileProjectRecordService fileProjectRecordService;

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
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long projectId) {
        Project project = getById(projectId);//先查询是否存在
        if(project==null||!Objects.equals(project.getUserId(), BaseContext.getUser().getId())){
            return false;
        }
        QueryWrapper<FileProjectRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId);
        List<FileProjectRecord> list = fileProjectRecordService.list(queryWrapper);
        List<Long> idList = list.stream().map(FileProjectRecord::getFileId).collect(Collectors.toList());
        if(idList.isEmpty())return removeById(projectId);
        //简单处理 todo
        fileService.deleteFiles(idList); // 删除项目下的所有文件
        fileProjectRecordService.remove(queryWrapper); // 删除项目下的所有翻译对记录
        translationPairsService.remove(new QueryWrapper<TranslationPairs>().in("file_id", idList)); // 删除翻译对记录
        return removeById(projectId);
    }

}




