package com.example.trans_backend_admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@MapperScan("com.example.trans_backend_admin.mapper")
@ComponentScan(basePackages = {"com.example.trans_backend_admin","com.example.trans_backend_common"})
public class TransBackendAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransBackendAdminApplication.class, args);
    }

}
