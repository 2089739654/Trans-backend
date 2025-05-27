package com.example.trans_backend_file.manager.translate.impl;


import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.manager.connection.impl.BaiduConnectionFactory;
import com.example.trans_backend_file.manager.translate.TranslateService;
import lombok.extern.slf4j.Slf4j;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;


@Component
@Slf4j
public class TranslateServiceByBaidu implements TranslateService {

    @Resource
    private BaiduConnectionFactory baiduConnectionFactory;

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(5));


    //已分配内存并赋值
    {
        threadPoolExecutor.setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("baidu-translate-pool-").setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error(e.getMessage());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "翻译失败");
            }
        }).build());
    }


    public String translateText(String text, String from, String to) {
        try {
            HttpClient httpClient = baiduConnectionFactory.getHttpClient();
            HttpGet httpGet = baiduConnectionFactory.getHttpGet(text, from, to);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            ThrowUtils.throwIf(entity == null, ErrorCode.SYSTEM_ERROR, "翻译失败");
            String responseString = EntityUtils.toString(entity, "UTF-8");
            JSONObject jsonResponse = new JSONObject(responseString);
            if (jsonResponse.has("trans_result")) {
                JSONArray transResult = jsonResponse.getJSONArray("trans_result");
                return transResult.getJSONObject(0).getString("dst");
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "翻译失败");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "翻译失败");
        }
    }


    @Override
    public List<String> translate(List<String> text, String from, String to) {

        int size = text.size();
        CountDownLatch countDownLatch=new CountDownLatch(size);
        List<String> result = new ArrayList<>(size);
        Set<Integer> hashset=new HashSet<>();
        Set<Integer> error=Collections.synchronizedSet(hashset);
        // 批量翻译
        for (int i = 0; i < size; i++) {
            int index= i;
            threadPoolExecutor.submit(() -> {
                try {
                    String string = translateText(text.get(index), from, to);
                    result.set(index, string);
                } catch (Exception e) {
                    error.add(index);
                    log.error("翻译失败, text={}, from={}, to={}, error={}", text.get(index), from, to, e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }

            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("线程池被打断");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"批量翻译线程池被打断");
        }
        //重试
        if(!error.isEmpty()){
            for (Integer index : error) {
                String string = text.get(index);
                String translatedText=null;
                try {
                    translatedText = translateText(string, from, to);
                } catch (Exception e) {
                    log.error("翻译失败, text={}, from={}, to={}, error={}", string, from, to, e.getMessage());
                    //再次翻译失败 通知管理员 todo
                } finally {
                    result.set(index, translatedText);
                }
            }
        }
        return result;
    }
}
