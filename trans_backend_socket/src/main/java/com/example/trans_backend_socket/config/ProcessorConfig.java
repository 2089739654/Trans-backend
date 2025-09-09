package com.example.trans_backend_socket.config;


import com.example.trans_backend_socket.processor.AuthProcessor;
import com.example.trans_backend_socket.processor.ProcessorChain;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessorConfig {

    @ConditionalOnMissingBean(ProcessorChain.class)
    @Bean
    public ProcessorChain processorChain() {
        ProcessorChain processorChain=new ProcessorChain();
        AuthProcessor authProcessor=new AuthProcessor();
        processorChain.addProcessor(authProcessor);
        return processorChain;
    }






}
