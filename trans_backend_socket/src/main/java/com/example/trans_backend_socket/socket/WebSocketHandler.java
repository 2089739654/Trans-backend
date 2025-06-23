package com.example.trans_backend_socket.socket;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_socket.context.WebSocketSessionContext;
import com.example.trans_backend_socket.disruptor.EditEventWorkProducer;
import com.example.trans_backend_socket.entity.EditEnums;
import com.example.trans_backend_socket.entity.TextEditMessage;
import com.example.trans_backend_socket.entity.UserVo;
import com.example.trans_backend_socket.processor.ProcessorChain;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Resource
    private ProcessorChain processorChain;


    @Resource
    private EditEventWorkProducer editEventWorkProducer;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 在连接建立后执行的逻辑
        System.out.println("连接建立");
        processorChain.process(session);
        // 发送用户加入的消息
        List<WebSocketSession> list = WebSocketSessionContext.getSession(getGroupId(session), session);
        User user = WebSocketSessionContext.getUser(session);
        TextEditMessage textEditMessage = new TextEditMessage();
        textEditMessage.setType(EditEnums.ENTER_EDIT.getValue());
        textEditMessage.setUser(BeanUtil.copyProperties(user, UserVo.class));
        broadcastMessage(list, textEditMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        TextEditMessage ms = JSONUtil.toBean(message.getPayload(), TextEditMessage.class);
        User user = WebSocketSessionContext.getUser(session);
        ms.setUser(BeanUtil.copyProperties(user, UserVo.class));
        editEventWorkProducer.publishEvent(ms, session, getGroupId(session));
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = WebSocketSessionContext.getUser(session);
        // 在连接关闭后执行的逻辑
        WebSocketSessionContext.removeSession(getGroupId(session), session);
        List<WebSocketSession> list = WebSocketSessionContext.getSession(getGroupId(session), session);
        // 发送用户离开的消息
        TextEditMessage textEditMessage = new TextEditMessage();
        textEditMessage.setType(EditEnums.EXIT_EDIT.getValue());
        textEditMessage.setUser(BeanUtil.copyProperties(user, UserVo.class));
        broadcastMessage(list,textEditMessage);
    }


    public Long getGroupId(WebSocketSession session) {
        String path = Objects.requireNonNull(session.getUri()).toString();
        return Long.valueOf(path.substring(path.lastIndexOf("?") + 1 + "groupId=".length()));
    }



    public void broadcastMessage(List<WebSocketSession> webSocketSessions, TextEditMessage textEditMessage) {
        if (webSocketSessions.isEmpty()) return;
        //发送消息
        for (WebSocketSession webSocketSession : webSocketSessions) {
            if (!webSocketSession.isOpen()) continue;
            try {
                String string = objectMapper.writeValueAsString(textEditMessage);
                TextMessage textMessage = new TextMessage(string);
                webSocketSession.sendMessage(textMessage);
            } catch (IOException e) {
                throw new BusinessException("消息发送失败",e, ErrorCode.SYSTEM_ERROR);
            }
        }


    }

    public void handleEnterEditMessage(TextEditMessage textEditMessage) {
    }

    public void handleExitEditMessage(TextEditMessage textEditMessage,Long groupId,WebSocketSession webSocketSession) {
        List<WebSocketSession> list = WebSocketSessionContext.getSession(groupId, webSocketSession);
        broadcastMessage(list, textEditMessage);
    }

    public void handleEditActionMessage(TextEditMessage textEditMessage) {
    }

    public void handleErrorMessage() {
    }
}
