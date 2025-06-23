package com.example.trans_backend_socket.processor;

import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_socket.context.WebSocketSessionContext;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

public class AuthProcessor extends Processor {

    @Override
    protected void doProcess(WebSocketSession webSocketSession) {
        String token = webSocketSession.getHandshakeHeaders().getFirst("token");
        User user = JSONUtil.toBean(token, User.class);
        String path = Objects.requireNonNull(webSocketSession.getUri()).toString();
        Long groupId = null;
        groupId = Long.valueOf(path.substring(path.lastIndexOf("?") + 1+"groupId=".length()));
        WebSocketSessionContext.addSession(groupId, webSocketSession, user);
    }
}
