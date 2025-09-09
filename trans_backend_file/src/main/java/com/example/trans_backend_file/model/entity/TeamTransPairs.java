package com.example.trans_backend_file.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName team_trans_pairs
 */
@TableName(value ="team_trans_pairs")
@Data
public class TeamTransPairs {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 
     */
    private Long fileId;

    /**
     * 
     */
    private String sourceText;

    /**
     * 
     */
    private String translatedText;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 
     */
    private Integer position;

    /**
     * 
     */
    private Long editorId;


    private Long timestamp;
}