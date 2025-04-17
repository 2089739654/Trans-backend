package com.example.trans_backend_admin.controller;


import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@RestController
@Api(tags = "测试接口")
public class TestController {


    @RequestMapping("/test")
    public String test(){
        return "test";
    }

    public void test1(HttpServletResponse response) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write("test".getBytes());
        return ;
    }



}
