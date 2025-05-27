package com.example.trans_backend_file.manager;


import com.example.trans_backend_file.enums.LanguageType;
import com.example.trans_backend_file.manager.translate.TranslateService;
import com.example.trans_backend_file.manager.translate.impl.TranslateServiceByBaidu;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class TranslationManager {

    @Resource
    private TranslateService translateService;

    public List<String> translate(List<String> text){
//        批量翻译
        return translateService.translate(text, LanguageType.ENGLISH.getCode(), LanguageType.CHINESE.getCode());
    }

    public String translate(String text) {
        return translateService.translateText(text, LanguageType.ENGLISH.getCode(), LanguageType.CHINESE.getCode());
    }


}
