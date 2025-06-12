package com.example.trans_backend_file.task;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.json.JSONUtil;
import com.example.trans_backend_file.config.MqConfig;
import com.example.trans_backend_file.model.entity.File;
import com.example.trans_backend_file.model.entity.SysRecord;
import com.example.trans_backend_file.service.SysRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class FileResolverTask implements CommandLineRunner, DisposableBean {

    @Resource
    private SysRecordService sysRecordService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private ScheduledThreadPoolExecutor threadPoolExecutor;
    {
        threadPoolExecutor=new ScheduledThreadPoolExecutor(3);
        threadPoolExecutor.setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("file-resolver-pool-").setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error(e.getMessage());
                throw new RuntimeException("文件解析定时任务失败");
            }
        }).build());
    }

    @Override
    public void run(String... args) throws Exception {

        threadPoolExecutor.scheduleAtFixedRate(()->{
            List<SysRecord> recordList = sysRecordService.getAll();
            for (SysRecord sysRecord:recordList){
                String content = sysRecord.getContent();
                File file = JSONUtil.toBean(content, File.class);
                try {
                    //可以异步发送
                    rabbitTemplate.convertAndSend("TransExchange", "resolve", MqConfig.getMessage(file, String.valueOf(sysRecord.getId())),MqConfig.getCorrelationData());
                } catch (AmqpException e) {
                    log.error("消息发送失败,信息id:"+sysRecord.getId());
                }
            }
        },0, 10, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        if(threadPoolExecutor!=null){
            threadPoolExecutor.shutdown();
        }
    }
}
