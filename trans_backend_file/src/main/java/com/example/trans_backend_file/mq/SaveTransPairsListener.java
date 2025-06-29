package com.example.trans_backend_file.mq;

import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.utils.JsonUtils;
import com.example.trans_backend_file.model.entity.SaveMessage;
import com.example.trans_backend_file.service.TeamTransPairsService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
@RabbitListener(queues = "SaveQueue")
public class SaveTransPairsListener {

    @Resource
    private TeamTransPairsService teamTransPairsService;


    @RabbitHandler(isDefault = true)
    public void saveTransPairs(Message message, Channel channel){
        byte[] body = message.getBody();
        String s = new String(body);
        SaveMessage saveMessage = JSONUtil.toBean(s, SaveMessage.class);
        boolean result = teamTransPairsService.trySave(saveMessage);
        if(!result)log.error("保存失败: {}", saveMessage);
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            throw new RuntimeException("消息确认失败", e);
        }
    }
}
