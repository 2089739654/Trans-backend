package com.example.trans_backend_file.manager;



import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


@Component
public class TranslateService {

    @Value("${baidu.appId}")
    private static String APP_ID;


    @Value("${baidu.secretKey}")
    private static String SECRET_KEY;

    private static final String API_URL = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    public  static  String translate(String text,String fromLang,String toLang){
        try {
            // 生成随机数
            Random random = new Random();
            int salt = random.nextInt(32768) + 32768;

            // 拼接签名原文
            String sign =APP_ID + text + salt + SECRET_KEY;
            String signMd5 = md5(sign);

            // 构建请求参数
            String query = "q=" + URLEncoder.encode(text, String.valueOf(StandardCharsets.UTF_8))
                    + "&from=" + fromLang
                    + "&to=" + toLang
                    + "&appid=" + APP_ID
                    + "&salt=" + salt
                    + "&sign=" + signMd5;

            // 发送 HTTP 请求
            URL url = new URL(API_URL + "?" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 获取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // 解析 JSON 响应
            JSONObject jsonResponse = new JSONObject(response.toString());
            if (jsonResponse.has("trans_result")) {
                JSONArray transResult = jsonResponse.getJSONArray("trans_result");
                return transResult.getJSONObject(0).getString("dst");
            } else {
                System.out.println("翻译出错: " + jsonResponse.optString("error_msg", "未知错误"));
                return null;
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"翻译失败");
        }

    }


    // MD5 加密方法
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
