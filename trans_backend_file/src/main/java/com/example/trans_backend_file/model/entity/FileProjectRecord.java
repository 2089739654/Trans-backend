package com.example.trans_backend_file.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 
 * @TableName file_project_record
 */
@TableName(value ="file_project_record")
public class FileProjectRecord {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 
     */
    private Long projectId;

    /**
     * 
     */
    private Long fileId;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    public Long getId() {
        return id;
    }

    /**
     * 
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 
     */
    public Long getProjectId() {
        return projectId;
    }

    /**
     * 
     */
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * 
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * 
     */
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    /**
     * 
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}