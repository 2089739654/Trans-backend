package com.example.trans_backend_socket.disruptor;

import com.example.trans_backend_socket.entity.DisruptorEvent;
import com.example.trans_backend_socket.entity.TextEditMessage;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
public class EditEventWorkProducer {


    @Resource
    @Lazy
    private Disruptor<DisruptorEvent> disruptor;

    public void publishEvent(TextEditMessage textEditMessage, WebSocketSession webSocketSession,Long groupId){
        RingBuffer<DisruptorEvent> ringBuffer = disruptor.getRingBuffer();
        long next = ringBuffer.next();
        DisruptorEvent disruptorEvent = ringBuffer.get(next);
        disruptorEvent.setTextEditMessage(textEditMessage);
        disruptorEvent.setWebSocketSession(webSocketSession);
        disruptorEvent.setGroupId(groupId);
        // 发布事件
        ringBuffer.publish(next);
    }


    @PreDestroy
    public void destroy() {
        disruptor.shutdown();
    }
}
