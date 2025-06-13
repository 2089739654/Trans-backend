package com.example.trans_backend_socket.socket;

import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_socket.processor.ProcessorChain;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Resource
    private ProcessorChain processorChain;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 在连接建立后执行的逻辑
        System.out.println("连接建立");
        processorChain.process(session);
        // 发送用户加入的消息
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println(Thread.currentThread().getName());
        System.out.println("Received message: " + message.getPayload());
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 在连接关闭后执行的逻辑

        // 发送用户离开的消息
    }


    public Long getGroupId(WebSocketSession session) {
        String path = Objects.requireNonNull(session.getUri()).getPath();
        Long groupId = Long.valueOf(path.substring(path.lastIndexOf("/") + 1 + "groupId=".length()));
        return groupId;
    }
}
