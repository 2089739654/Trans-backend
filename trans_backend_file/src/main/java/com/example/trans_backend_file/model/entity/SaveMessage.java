package com.example.trans_backend_file.model.entity;

import lombok.Data;

@Data
public class SaveMessage {

    private Long userId;

    private Long transId;

    private String transText;

    private Long timestamp;
}
