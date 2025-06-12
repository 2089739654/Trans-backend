package com.example.trans_backend_admin.controller;

import com.example.trans_backend_admin.service.GroupService;
import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.common.ResultUtils;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class GroupController {

    @Resource
    private GroupService groupService;


    @PostMapping("/createGroup")
    public BaseResponse<String> createGroup(String groupName){
        ThrowUtils.throwIf(groupName == null || groupName.isEmpty(), ErrorCode.PARAMS_ERROR, "群组名称不能为空");
        Long id = BaseContext.getUser().getId();
        groupService.createGroup(groupName, id);
        return ResultUtils.success("success");
    }

    @PostMapping("/addUserToGroup")
    public BaseResponse<String> addUserToGroup(Long groupId, Long userId) {
        ThrowUtils.throwIf(groupId == null || userId == null, ErrorCode.PARAMS_ERROR, "群组ID或用户ID不能为空");
        groupService.addUserToGroup(groupId, userId);
        return ResultUtils.success("success");
    }






}
