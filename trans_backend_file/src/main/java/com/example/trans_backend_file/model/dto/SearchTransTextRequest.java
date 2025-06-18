package com.example.trans_backend_file.model.dto;

import lombok.Data;

@Data
public class SearchTransTextRequest {

    private Long transId;

    private String text;

}
