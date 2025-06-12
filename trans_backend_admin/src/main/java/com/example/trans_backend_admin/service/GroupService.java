package com.example.trans_backend_admin.service;

import com.example.trans_backend_admin.model.entity.Group;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 20897
* @description 针对表【group】的数据库操作Service
* @createDate 2025-06-11 17:28:19
*/
public interface GroupService extends IService<Group> {

    void createGroup(String groupName, Long creatorId);

    void addUserToGroup(Long groupId, Long userId);
}
