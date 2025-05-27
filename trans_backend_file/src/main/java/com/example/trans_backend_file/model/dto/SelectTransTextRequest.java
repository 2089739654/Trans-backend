package com.example.trans_backend_file.model.dto;


import lombok.Data;

@Data
public class SelectTransTextRequest {

    private Integer position;

    private Integer size;

    private Integer currentPage;

    private Long fileId;


}
