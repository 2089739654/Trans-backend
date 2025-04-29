package com.example.trans_backend_file.manager.connection.impl;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class BaiduConnectionFactory extends AbstractConnectionFactory{

    @Value("${baidu.appId}")
    private static String APP_ID;


    @Value("${baidu.secretKey}")
    private static String SECRET_KEY;

    private static final String API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    private final PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();

    public BaiduConnectionFactory(@Value("${baidu.connection.maxTotal:50}") int maxTotal, @Value("${baidu.connection.maxPerRoute:20}") int maxPerRoute) {
        this.httpClientConnectionManager.setMaxTotal(maxTotal);
        this.httpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
    }


    @Override
    public HttpClient buildHttpClient() {
        return HttpClientBuilder.create().setConnectionManager(httpClientConnectionManager).
                setDefaultRequestConfig(requestConfig).build();
    }

    public HttpGet getHttpGet(String text) throws UnsupportedEncodingException {
        // 生成随机数
        Random random = new Random();
        int salt = random.nextInt(32768) + 32768;
        // 拼接签名原文
        String sign = APP_ID + text + salt + SECRET_KEY;
        String signMd5 = md5(sign);
        // 构建请求参数
        String query = "q=" + URLEncoder.encode(text, "UTF-8")
                + "&from=auto"
                + "&to=zh"
                + "&appid=" + APP_ID
                + "&salt=" + salt
                + "&sign=" + signMd5;
        HttpGet httpGet = new HttpGet(API_URL + "?" + query);
        return httpGet;
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }




}
