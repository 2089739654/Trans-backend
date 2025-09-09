package com.example.trans_backend_socket.entity;


import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

@Data
public class DisruptorEvent {

    private TextEditMessage textEditMessage; // 文本编辑消息

    private Long groupId;

    private WebSocketSession webSocketSession;



}
