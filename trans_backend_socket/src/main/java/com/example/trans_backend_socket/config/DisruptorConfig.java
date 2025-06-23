package com.example.trans_backend_socket.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.example.trans_backend_socket.disruptor.EditEventWorkHandler;
import com.example.trans_backend_socket.entity.DisruptorEvent;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class DisruptorConfig {

    @Resource
    private EditEventWorkHandler editEventWorkHandler;

    @Bean
    public Disruptor<DisruptorEvent> disruptor() {
        // 定义 ringBuffer 的大小
        int bufferSize = 1024 * 256;
        // 创建 disruptor
        Disruptor<DisruptorEvent> disruptor = new Disruptor<>(
                DisruptorEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("disruptorEventDisruptor").build()
        );
        // 启动 disruptor
        disruptor.start();
        disruptor.handleEventsWithWorkerPool(editEventWorkHandler);
        return disruptor;
    }


}
