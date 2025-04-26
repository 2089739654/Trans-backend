package com.example.trans_backend_file.model.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
public class TranslationPairs {

    private String id;

    private Long fileId;

    private String sourceText;

    private String translatedText;

    private Float[] vector;

    private Date createTime;

    private Integer version;

    private Boolean isNew;


}
