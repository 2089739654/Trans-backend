package com.example.trans_backend_admin.model.dto;

import lombok.Data;

@Data
public class AddUserToGroupRequest {
    private Long groupId;
    private Long userId;
}
