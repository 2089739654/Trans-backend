package com.example.trans_backend_socket.socket;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_file.model.entity.TeamTransPairs;
import com.example.trans_backend_socket.context.WebSocketSessionContext;
import com.example.trans_backend_socket.disruptor.EditEventWorkProducer;
import com.example.trans_backend_socket.entity.EditEnums;
import com.example.trans_backend_socket.entity.TextEditMessage;
import com.example.trans_backend_socket.entity.UserVo;
import com.example.trans_backend_socket.processor.ProcessorChain;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    @Resource
    private ProcessorChain processorChain;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


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
        broadcastMessage(list, textEditMessage);
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
                throw new BusinessException("消息发送失败", e, ErrorCode.SYSTEM_ERROR);
            }
        }


    }

    public void handleEnterEditMessage(TextEditMessage textEditMessage) {
    }

    public void handleExitEditMessage(TextEditMessage textEditMessage, Long groupId, WebSocketSession webSocketSession) {
        saveOrUpdateRedis(textEditMessage);
        List<WebSocketSession> list = WebSocketSessionContext.getSession(groupId, webSocketSession);
        broadcastMessage(list, textEditMessage);
    }

    private void saveOrUpdateRedis(TextEditMessage textEditMessage) {
        Long fileId = textEditMessage.getFileId();

        String key = "file:" + fileId;
        List<String> keys = Collections.singletonList(key);


        List<String> args = Arrays.asList(
                textEditMessage.getPosition().toString(),
                textEditMessage.getTransId().toString(),
                textEditMessage.getUser().getId().toString(),
                textEditMessage.getSourceText(),
                textEditMessage.getTransText(),
                textEditMessage.getTimestamp().toString(),
                String.valueOf(3 * 24 * 60 * 60) // 3天的秒数
        );


        // 执行Lua脚本
        Long result = (Long) stringRedisTemplate.execute(
                new DefaultRedisScript<>(SAVE_OR_UPDATE_SCRIPT, Long.class),
                keys,
                args.toArray(new Object[0])
        );

        // 处理结果
        if (result != null && result == 1) {
            // 更新成功
        } else {
            // 未更新（时间戳较旧）
            log.info("未更新，当前时间戳较旧，文件ID: {}, 编辑位置: {}", fileId, textEditMessage.getPosition());
        }
    }

    public void handleEditActionMessage(TextEditMessage textEditMessage) {
    }

    public void handleErrorMessage() {
    }

    private final String SAVE_OR_UPDATE_SCRIPT =
            "local currentJson = redis.call('HGET', KEYS[1], ARGV[1])\n" +
                    "local currentTimestamp = 0\n" +
                    "if currentJson ~= false then\n" +
                    "    local currentObj = cjson.decode(currentJson)\n" +
                    "    currentTimestamp = tonumber(currentObj.timestamp)\n" +
                    "end\n" +
                    "local newTimestamp = tonumber(ARGV[6])\n" +
                    "if currentTimestamp < newTimestamp then\n" +
                    "    local newObj = {\n" +
                    "        id = tonumber(ARGV[2]),\n" +
                    "        editorId = tonumber(ARGV[3]),\n" +
                    "        position = tonumber(ARGV[1]),\n" +
                    "        fileId = tonumber(string.sub(KEYS[1], 6)),\n" +
                    "        sourceText = ARGV[4],\n" +
                    "        translatedText = ARGV[5],\n" +
                    "        timestamp = newTimestamp,\n" +
                    "        updateTime = os.date('%Y-%m-%dT%H:%M:%S')\n" +
                    "    }\n" +
                    "    local newJson = cjson.encode(newObj)\n" +
                    "    redis.call('HSET', KEYS[1], ARGV[1], newJson)\n" +
                    "    redis.call('EXPIRE', KEYS[1], ARGV[7])\n" +
                    "    return 1\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";
}
