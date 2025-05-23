package com.example.trans_backend_file.controller;

import com.aspose.pdf.operators.Re;
import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.common.ResultUtils;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.GlobalExceptionHandler;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.model.entity.Project;
import com.example.trans_backend_file.service.FileService;
import com.example.trans_backend_file.service.ProjectService;
import io.swagger.annotations.ApiOperation;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/project")
public class ProjectController {
    @Resource
    ProjectService projectService;

    /**
     * 根据用户Id查询用户创建的所有项目
     * @return
     */
    @GetMapping("/projects")
    public BaseResponse<List<Project>> allProjects(){
        return ResultUtils.success(projectService.selectListById(BaseContext.getUser().getId()));
    }

    /**
     *创建项目
     * @param name
     * @return
     */
    @PostMapping("/insert")
    @ApiOperation(value = "创建成功返回项目ID，失败则返回errorcode")
    public BaseResponse<?> createProject(@NotNull String name){
        Project project = projectService.create(BaseContext.getUser().getId(), name);
        if(project!=null){
            return ResultUtils.success(project.getId());
        }else{
            return  ResultUtils.error(ErrorCode.PROJECT_ERROR);
//            return new BaseResponse<ErrorCode>(ErrorCode.PROJECT_ERROR);
        }
    }

    /**
     * 修改项目（名称等）
     * @param name
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "修改项目，接受项目id,已经修改的项目名称")
    public BaseResponse<ErrorCode> updateProject(@NotNull Long projectId,@NotNull String name) {
        boolean update = projectService.update(projectId, name);
        if (update){
            return ResultUtils.success(ErrorCode.SUCCESS);
        }else {
            return ResultUtils.error(ErrorCode.PROJECT_UPDATE_ERROR);
        }
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除已经创建的项目")
    public BaseResponse<ErrorCode> deleteProject(@NotNull Long projectId) {
        boolean delete = projectService.delete(projectId);
        if (delete){
            return ResultUtils.success(ErrorCode.SUCCESS);
        }else {
            return ResultUtils.error(ErrorCode.PROJECT_DELETE_ERROR);
        }
    }

}
