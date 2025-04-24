package com.example.trans_backend_file.config;


import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Random;
import java.util.UUID;

@Configuration
@Slf4j
public class MqConfig {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            String messageId = returnedMessage.getMessage().getMessageProperties().getMessageId();
            log.error("监听到消息:"+messageId+"return callback");
            log.debug("exchange：{}", returnedMessage.getExchange());
            log.debug("routingKey：{}", returnedMessage.getRoutingKey());
            log.debug("message：{}", returnedMessage.getMessage());
            log.debug("replyCode：{}", returnedMessage.getReplyCode());
            log.debug("replyText：{}", returnedMessage.getReplyText());
        });
    }

    //消息队列配置回调函数
    public static CorrelationData getCorrelationData(){
        String id= UUID.randomUUID().toString();
        CorrelationData cd=new CorrelationData(id);
        cd.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息:"+id+"发送失败:"+e.getMessage());
            }

            @Override
            public void onSuccess(CorrelationData.Confirm result) {
                if(result.isAck()){
                    //只要消息投递到exchange就ack
                    log.info("消息:"+id+"已投递到exchange");
                }else {
                    //没有相应的exchange
                    log.error("消息:"+id+"未投递到exchange");
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"消息:"+id+"未投递到exchange:"+result.getReason());
                }
            }
        });

        return cd;
    }

    //生成message
    public static Message getMessage(Object object,String MessageId){
        Message message=new Message(JSONUtil.toJsonStr(object).getBytes());
        message.getMessageProperties().setMessageId(MessageId);
        return message;
    }


}
