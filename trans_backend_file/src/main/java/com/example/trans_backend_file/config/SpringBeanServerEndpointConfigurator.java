package com.example.trans_backend_file.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.websocket.server.ServerEndpointConfig;

//@Component // 将此 Configurator 声明为 Spring Bean，以便 Spring 注入 ApplicationContext
public class SpringBeanServerEndpointConfigurator extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanServerEndpointConfigurator.context = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (SpringBeanServerEndpointConfigurator.context == null) {
            throw new InstantiationException(
                    "ApplicationContext not available in SpringBeanServerEndpointConfigurator. " +
                    "Ensure this configurator is managed as a Spring bean and properly initialized.");
        }
        // 从 Spring 上下文中获取 endpointClass 类型的单例 Bean
        return SpringBeanServerEndpointConfigurator.context.getBean(endpointClass);
    }
}