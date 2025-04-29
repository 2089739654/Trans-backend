package com.example.trans_backend_file.manager.connection.impl;

import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_common.exception.ThrowUtils;
import com.example.trans_backend_file.manager.connection.impl.AbstractConnectionFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliYunConnectionFactory extends AbstractConnectionFactory {

    private static final String BASE_URL="https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";

    private final String apiKey;

    public AliYunConnectionFactory(@Value("${aliyun.apiKey}") String apiKey,@Value("${aliyun.connection.maxTotal:100}") int maxTotal,@Value("${aliyun.connection.maxPerRoute:20}")int maxPerRoute) {
        ThrowUtils.throwIf(apiKey==null, ErrorCode.SYSTEM_ERROR,"请配置阿里云API密钥");
        this.apiKey = apiKey;
        this.httpClientConnectionManager.setMaxTotal(maxTotal);
        this.httpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
    }

    private final PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();

    @Override
    public HttpClient buildHttpClient() {
        return HttpClientBuilder.create().setConnectionManager(httpClientConnectionManager).
                setDefaultRequestConfig(requestConfig).build();
    }

    public HttpPost getHttpPost() {
        HttpPost httpPost = new HttpPost(BASE_URL);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + apiKey);
        httpPost.setConfig(requestConfig);
        return httpPost;
    }


}
