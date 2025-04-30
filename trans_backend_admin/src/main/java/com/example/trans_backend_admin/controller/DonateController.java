package com.example.trans_backend_admin.controller;

import com.example.trans_backend_common.common.BaseResponse;
import com.example.trans_backend_common.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@RestController
@Slf4j
public class DonateController {
    @GetMapping("/donate")
    public BaseResponse<String> donate() throws IOException {
//        File file = new File("H:\\PROject\\Trans-backend\\trans_backend_admin\\src\\main\\resources\\Pictures\\donate.jpg");
        InputStream resourceAsStream = DonateController.class.getClassLoader().getResourceAsStream("Pictures/donate.jpg");
        byte[] bytes = StreamUtils.copyToByteArray(resourceAsStream);
        String s = Base64.getEncoder().encodeToString(bytes);
        return ResultUtils.success(s);
    }
}

