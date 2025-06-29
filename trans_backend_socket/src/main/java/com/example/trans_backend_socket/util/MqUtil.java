package com.example.trans_backend_socket.util;

import com.example.trans_backend_file.config.MqConfig;
import com.example.trans_backend_file.model.entity.SaveMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MqUtil {

    @Resource
    private RabbitTemplate rabbitTemplate;


    private static final String EXCHANGE="SaveExchange";

    private static final String ROUTING_KEY="save";

    public void sendMessage(SaveMessage saveMessage) {
        Message message = MqConfig.getMessage(saveMessage, saveMessage.getTransId() + "_" + saveMessage.getTimestamp());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message, MqConfig.getCorrelationData());
    }




}
