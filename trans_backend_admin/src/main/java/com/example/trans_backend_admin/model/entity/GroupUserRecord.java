package com.example.trans_backend_admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName group_user_record
 */
@TableName(value ="group_user_record")
@Data
public class GroupUserRecord {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 
     */
    private Long groupId;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private Date createTime;


}