package com.example.trans_backend_file.model.vo;


import lombok.Data;

@Data
public class SelectTransPairsVo {
    private Long id;
    private Long fileId;
    private String fileName;
    private String sourceText;
    private String translatedText;
}
