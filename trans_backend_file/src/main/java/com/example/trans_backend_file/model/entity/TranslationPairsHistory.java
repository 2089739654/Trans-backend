package com.example.trans_backend_file.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName translation_pairs_history
 */
@TableName(value ="translation_pairs_history")
public class TranslationPairsHistory {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 
     */
    private Long transId;

    /**
     * 
     */
    private Integer version;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private String translatedText;

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
    public Long getTransId() {
        return transId;
    }

    /**
     * 
     */
    public void setTransId(Long transId) {
        this.transId = transId;
    }

    /**
     * 
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * 
     */
    public void setVersion(Integer version) {
        this.version = version;
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

    /**
     * 
     */
    public String getTranslatedText() {
        return translatedText;
    }

    /**
     * 
     */
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}