package com.example.trans_backend_admin.model.dto;


import lombok.Data;

@Data
public class DeleteUserFromGroupRequest {

    private Long groupId;
    private Long userId;
}
