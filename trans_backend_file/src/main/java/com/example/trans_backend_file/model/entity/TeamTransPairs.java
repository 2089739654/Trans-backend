package com.example.trans_backend_file.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 
     */
    private Integer position;

    /**
     * 
     */
    private Long editorId;

}