package com.example.trans_backend_admin.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest  {


    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     *  昵称
     */
    private String userName;

}
