package com.example.trans_backend_file.model.dto;

import lombok.Data;

@Data
public class GetTranslationHistoryRequest {
    private Long transId;

    private Integer version;
}
