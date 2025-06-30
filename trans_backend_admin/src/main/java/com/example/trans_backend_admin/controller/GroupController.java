package com.example.trans_backend_admin.controller;

import com.example.trans_backend_admin.model.dto.AddUserToGroupRequest;
import com.example.trans_backend_admin.model.dto.DeleteUserFromGroupRequest;
import com.example.trans_backend_admin.model.entity.Group;
import com.example.trans_backend_admin.service.GroupService;
import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.common.ResultUtils;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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
    public BaseResponse<String> addUserToGroup(@RequestBody AddUserToGroupRequest addUserToGroupRequest) {
        Long groupId = addUserToGroupRequest.getGroupId();

        Long userId = addUserToGroupRequest.getUserId();
        ThrowUtils.throwIf(groupId == null || userId == null, ErrorCode.PARAMS_ERROR, "群组ID或用户ID不能为空");
        groupService.addUserToGroup(groupId, userId);
        return ResultUtils.success("success");
    }


    @GetMapping("/getGroupList")
    public BaseResponse<List<Group>> getGroupList() {
        Long userId = BaseContext.getUser().getId();
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        List<Group> groupList = groupService.getGroupListByUserId(userId);
        return ResultUtils.success(groupList);

    }


    @GetMapping("/getUserByGroupId")
    public BaseResponse<List<User>> getUserByGroupId(Long groupId){
        ThrowUtils.throwIf(groupId == null, ErrorCode.PARAMS_ERROR, "群组ID不能为空");
        List<User> userByGroupId = groupService.getUserByGroupId(groupId);
        return ResultUtils.success(userByGroupId);
    }

    @PostMapping("/deleteUserFromGroup")
    public BaseResponse<?> deleteUserFromGroup(@RequestBody DeleteUserFromGroupRequest deleteUserFromGroupRequest) {
        ThrowUtils.throwIf(deleteUserFromGroupRequest.getGroupId() == null || deleteUserFromGroupRequest.getUserId() == null, ErrorCode.PARAMS_ERROR, "群组ID或用户ID不能为空");
        groupService.deleteUserFromGroup(deleteUserFromGroupRequest.getGroupId(), deleteUserFromGroupRequest.getUserId());
        return ResultUtils.success("success");
    }

}
