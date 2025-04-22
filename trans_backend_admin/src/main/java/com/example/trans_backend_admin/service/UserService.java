package com.example.trans_backend_admin.service;

import com.example.trans_backend_common.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 20897
* @description 针对表【user】的数据库操作Service
* @createDate 2025-04-16 22:19:15
*/
public interface UserService extends IService<User> {

    User userRegister(String userAccount, String userPassword, String userName);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);
}
