package com.example.trans_backend_file.mq;

import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.manager.ResolveContext;
import com.example.trans_backend_file.manager.resolve.impl.ResolveManager;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.model.entity.SysRecord;
import com.example.trans_backend_file.service.SysRecordService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@RabbitListener(queues = "ResolveQueue")
@Service
public class FileResolveListener {


    @Resource
    private ResolveContext resolveContext;

    @Resource
    private SysRecordService sysRecordService;

    @Resource
    private RedissonClient redissonClient;


    @RabbitHandler(isDefault = true)
    public void resolveFile(Message message, Channel channel){
        String recordId = message.getMessageProperties().getMessageId();
        ThrowUtils.throwIf(recordId == null, ErrorCode.SYSTEM_ERROR, "消息ID不能为空");
        RLock lock = redissonClient.getLock("resolveFileLock:" + recordId);
        if (!lock.tryLock()){
            log.info("重复提交:"+recordId);
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("消息确认失败", e);
                throw new RuntimeException(e);
            }
            return;
        }
        SysRecord record = sysRecordService.getById(Long.valueOf(recordId));
        ThrowUtils.throwIf(record==null,ErrorCode.PARAMS_ERROR,"recordId错误");
        if (record.getStatus() == 1) {
            log.info("消息已处理，跳过处理: {}", recordId);
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("消息确认失败", e);
                throw new RuntimeException(e);
            }
            return;
        }
        String s=new String(message.getBody());
        File file = JSONUtil.toBean(s, File.class);
        log.info("接收消息：{}", file);
        String fileExtension = file.getFileExtension();
        try {
            ResolveManager resolveService = resolveContext.getResolveService(fileExtension);
            resolveService.resolve(file);
            sysRecordService.success(Long.valueOf(message.getMessageProperties().getMessageId()));
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            if (sysRecordService.select(Long.valueOf(message.getMessageProperties().getMessageId()))>=3) {
                log.error("文件解析失败超过3次，停止解析");
                try {
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                } catch (IOException ex) {
                    log.error("消息拒绝失败", ex);
                    throw new RuntimeException(ex);
                }
                return;
            }
            sysRecordService.error(Long.valueOf(message.getMessageProperties().getMessageId()));
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            } catch (IOException ex) {
                log.error("消息nack失败", ex);
                throw new RuntimeException(ex);
            }
            log.error(e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析文件失败");
        }
    }


}
