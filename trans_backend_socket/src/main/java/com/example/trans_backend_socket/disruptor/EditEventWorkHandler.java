package com.example.trans_backend_socket.disruptor;

import com.example.trans_backend_socket.entity.DisruptorEvent;
import com.example.trans_backend_socket.entity.EditEnums;
import com.example.trans_backend_socket.entity.TextEditMessage;
import com.example.trans_backend_socket.socket.WebSocketHandler;
import com.lmax.disruptor.WorkHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Component
public class EditEventWorkHandler implements WorkHandler<DisruptorEvent> {


    @Resource
    private WebSocketHandler webSocketHandler;


    @Override
    public void onEvent(DisruptorEvent event) throws Exception {
        TextEditMessage textEditMessage = event.getTextEditMessage();
        EditEnums type = EditEnums.getByType(textEditMessage.getType());
        if(type == null){
            // 如果类型不合法，直接返回错误
            webSocketHandler.handleErrorMessage();
            return;
        }
        Long groupId = event.getGroupId();
        WebSocketSession webSocketSession = event.getWebSocketSession();
        switch (type) {
            case ENTER_EDIT:
                webSocketHandler.handleEnterEditMessage(textEditMessage);
                break;
            case EXIT_EDIT:
                webSocketHandler.handleExitEditMessage(textEditMessage, groupId,webSocketSession);
                break;
            case EDIT_ACTION:
                webSocketHandler.handleEditActionMessage(textEditMessage);
                break;
            default:
                // 其他消息类型，返回错误提示
                webSocketHandler.handleErrorMessage();
                break;
        }


    }
}
