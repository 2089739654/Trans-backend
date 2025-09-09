package com.example.trans_backend_gateway;

import com.example.trans_backend_gateway.config.IpRateConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
})
@EnableConfigurationProperties(value = IpRateConfig.class)
public class TransBackendGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransBackendGatewayApplication.class, args);
    }

}
