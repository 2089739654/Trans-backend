package com.example.trans_backend_file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.example.trans_backend_file.mapper")
@ComponentScan(basePackages = {"com.example.trans_backend_file","com.example.trans_backend_common"})
public class TransBackendFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransBackendFileApplication.class, args);
    }

}
