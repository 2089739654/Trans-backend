package com.example.trans_backend_socket.socket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {


        System.out.println("WebSocket connection established: " + session.getId());
        System.out.println(Thread.currentThread().getName());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println(Thread.currentThread().getName());
        System.out.println("Received message: " + message.getPayload());
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("连接结束");
        System.out.println(Thread.currentThread().getName());
    }
}
