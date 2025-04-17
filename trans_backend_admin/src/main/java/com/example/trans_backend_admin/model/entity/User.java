package com.example.trans_backend_admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账户
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户权限：admin/user
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户头像
     */
    private String userName;

    /**
     *  用户头像
     */
    private String userAvatar;

    private String token;

}