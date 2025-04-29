package com.example.trans_backend_gateway.config;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.ServerCodecConfigurer;

public class ExceptionConfig {
    @Bean
    public ErrorProperties errorProperties(){
        return new ErrorProperties();
    }

    @Bean
    public WebProperties.Resources resources(){
        return new WebProperties.Resources();
    }

    @Bean
    public ErrorWebExceptionHandler errorExceptionHandler(ServerCodecConfigurer serverCodecConfigurer, ErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext){
        CustomErrorWebHandler errorExceptionHandler = new CustomErrorWebHandler(errorAttributes, resources, errorProperties, applicationContext);
        errorExceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        errorExceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return errorExceptionHandler;
    }
}
