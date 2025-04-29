package com.example.trans_backend_file.manager.translate.impl;


import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.manager.connection.impl.BaiduConnectionFactory;
import com.example.trans_backend_file.manager.translate.TranslateService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;


@Component
public class TranslateServiceByBaidu implements TranslateService {

    @Resource
    private BaiduConnectionFactory baiduConnectionFactory;

    public  String translateText(String text) {
        try {
            HttpClient httpClient = baiduConnectionFactory.getHttpClient();
            HttpGet httpGet=baiduConnectionFactory.getHttpGet(text);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            ThrowUtils.throwIf(entity == null, ErrorCode.SYSTEM_ERROR,"翻译失败");
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONObject jsonResponse = new JSONObject(responseString);
            if (jsonResponse.has("trans_result")) {
                JSONArray transResult = jsonResponse.getJSONArray("trans_result");
                return transResult.getJSONObject(0).getString("dst");
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"翻译失败");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"翻译失败");
        }
    }


    @Override
    public List<String> translate(List<String> text, String from, String to) {
        // 批量翻译



        return null;
    }
}
