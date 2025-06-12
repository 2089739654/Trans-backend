package com.example.trans_backend_file.model.dto;

import lombok.Data;

@Data
public class SearchTransTextRequest {

    private Long fileId;

    private String text;

}
