package com.example.trans_backend_file.manager.vectorization.impl;

import com.alibaba.dashscope.utils.JsonUtils;
import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_file.manager.connection.impl.AliYunConnectionFactory;
import com.example.trans_backend_file.manager.vectorization.VectorizationService;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class VectorizationByAliYun implements VectorizationService {

    @Resource
    private AliYunConnectionFactory aliYunConnectionFactory;


    @Override
    public Float[] vectorize(String text) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "text-embedding-v3");
        requestBody.put("input", text);
        requestBody.put("dimensions", 1024);
        requestBody.put("encoding_format", "float");
        String json = JsonUtils.toJson(requestBody);
        HttpClient httpClient = aliYunConnectionFactory.getHttpClient();
        HttpPost httpPost = aliYunConnectionFactory.getHttpPost();
        httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
        HttpResponse response = httpClient.execute(httpPost);
        String result = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (200 == statusCode) {
            result = EntityUtils.toString(response.getEntity());
        } else {
            log.info("请求第三方接口出现错误，状态码为:{}", statusCode);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求第三方接口出现错误，状态码为:" + statusCode);
        }
        String[] split = result.split(",");
        Float[] array = Arrays.stream(split).map(Float::valueOf).toArray(Float[]::new);

        httpPost.abort();
        HttpClientUtils.closeQuietly(httpClient);
        return array;
    }
}
