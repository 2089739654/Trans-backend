package com.example.trans_backend_file.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName project
 */
@TableName(value ="project")
@Data
public class Project {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private Date createTime;

    private Long groupId;
}