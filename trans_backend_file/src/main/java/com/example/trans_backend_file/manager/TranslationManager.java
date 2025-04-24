package com.example.trans_backend_file.manager;


import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class TranslationManager {

    @Resource
    private TranslateService translateService;

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static void translate(List<String> text){
//        批量翻译



    }


}
