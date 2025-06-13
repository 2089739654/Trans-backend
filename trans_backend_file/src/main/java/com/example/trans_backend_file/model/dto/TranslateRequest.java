package com.example.trans_backend_file.model.dto;

import lombok.Data;

@Data
public class TranslateRequest {

    private String sourceText;

    private Long fileId;
}
