package com.example.trans_backend_admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName group
 */
@TableName(value ="group")
@Data
public class Group {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    /**
     * 
     */
    private Long creatorId;

    /**
     * 
     */
    private Date createTime;

}