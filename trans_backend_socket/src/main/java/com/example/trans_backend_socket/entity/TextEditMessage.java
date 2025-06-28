package com.example.trans_backend_socket.entity;


import com.example.trans_backend_common.entity.User;
import lombok.Data;

@Data
public class TextEditMessage {


    private String type; // 消息类型，例如 "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"

    private UserVo user; // 用户信息

    private Long fileId; // 文件ID

    private Long transId;

    private String transText;


}
