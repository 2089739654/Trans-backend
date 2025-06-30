package com.example.trans_backend_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_admin.model.entity.Group;
import com.example.trans_backend_admin.model.entity.GroupUserRecord;
import com.example.trans_backend_admin.service.GroupService;
import com.example.trans_backend_admin.mapper.GroupMapper;
import com.example.trans_backend_admin.service.GroupUserRecordService;
import com.example.trans_backend_common.context.BaseContext;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 20897
* @description 针对表【group】的数据库操作Service实现
* @createDate 2025-06-11 17:28:19
*/
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group>
    implements GroupService{

    @Resource
    private GroupUserRecordService groupUserRecordService;

    @Resource
    private GroupMapper groupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createGroup(String groupName, Long creatorId){
        QueryWrapper<Group> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("name", groupName);
        queryWrapper.eq("creator_id", creatorId);
        Long count = baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR,"该群组已存在");
        Group group = new Group();
        group.setName(groupName);
        group.setCreatorId(creatorId);
        int insert = baseMapper.insert(group);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.OPERATION_ERROR, "创建群组失败");
        // 添加群组用户记录
        GroupUserRecord groupUserRecord = new GroupUserRecord();
        groupUserRecord.setGroupId(group.getId());
        groupUserRecord.setUserId(creatorId);
        boolean save = groupUserRecordService.save(groupUserRecord);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建群组用户记录失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserToGroup(Long groupId, Long userId) {
        // 检查群组是否存在
        Group group = baseMapper.selectById(groupId);
        ThrowUtils.throwIf(group == null, ErrorCode.PARAMS_ERROR, "群组不存在");
        ThrowUtils.throwIf(!group.getCreatorId().equals(BaseContext.getUser().getId()), ErrorCode.PARAMS_ERROR, "无权添加用户到群组");
        QueryWrapper<GroupUserRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("user_id", userId);
        long count = groupUserRecordService.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "用户已在群组中");
        GroupUserRecord groupUserRecord = new GroupUserRecord();
        groupUserRecord.setGroupId(groupId);
        groupUserRecord.setUserId(userId);

        boolean save = false;
        try {
            save = groupUserRecordService.save(groupUserRecord);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("用户已在群组中", e,ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "添加用户到群组失败");
    }

    @Override
    public List<Group> getGroupListByUserId(Long userId) {
        return groupMapper.getGroupByUserId(userId);
    }

    @Override
    public List<User> getUserByGroupId(Long groupId) {
        return groupMapper.getUserByGroupId(groupId);
    }

    @Override
    public void deleteUserFromGroup(Long groupId, Long userId) {
        Long id = BaseContext.getUser().getId();
        // 检查群组是否存在
        QueryWrapper<Group> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", groupId);
        Group res = baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(res == null, ErrorCode.PARAMS_ERROR, "群组不存在");
        ThrowUtils.throwIf(!res.getCreatorId().equals(id), ErrorCode.PARAMS_ERROR, "只有群主才能删除用户");

        // 检查用户是否在群组中
        QueryWrapper<GroupUserRecord> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("group_id", groupId);
        queryWrapper1.eq("user_id", userId);
        long count = groupUserRecordService.count(queryWrapper1);
        ThrowUtils.throwIf(count <= 0, ErrorCode.PARAMS_ERROR, "用户不在群组中");

        // 删除用户记录
        boolean remove = groupUserRecordService.remove(queryWrapper1);
        ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR, "删除用户失败");


    }
}




