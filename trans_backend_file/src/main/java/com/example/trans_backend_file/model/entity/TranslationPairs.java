package com.example.trans_backend_file.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.trans_backend_file.mybatisPlusHandler.FloatArrayTypeHandler;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "translation_pairs")
public class TranslationPairs {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long fileId;

    private String sourceText;

    private String translatedText;

    @TableField(typeHandler = FloatArrayTypeHandler.class)
    private Float[] vector;

    private Date createTime;

    private Integer version;

    private Boolean isNew;

    //文档中的相对位置
    private Integer position;


}
