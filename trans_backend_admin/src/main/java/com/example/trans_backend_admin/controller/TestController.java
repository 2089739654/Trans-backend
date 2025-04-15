package com.example.trans_backend_admin.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class TestController {


    @RequestMapping("/test")
    public String test(){

        return "test";
    }





}
