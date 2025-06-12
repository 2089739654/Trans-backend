package com.example.trans_backend_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.trans_backend_admin.model.entity.Group;
import com.example.trans_backend_admin.model.entity.GroupUserRecord;
import com.example.trans_backend_admin.service.GroupService;
import com.example.trans_backend_admin.mapper.GroupMapper;
import com.example.trans_backend_admin.service.GroupUserRecordService;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
}




