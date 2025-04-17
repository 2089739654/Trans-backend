package com.example.trans_backend_admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.trans_backend_admin.mapper")
public class TransBackendAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransBackendAdminApplication.class, args);
    }

}
