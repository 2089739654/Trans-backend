package com.example.trans_backend_file.manager.connection.impl;

import com.example.trans_backend_common.exception.BusinessException;
import com.example.trans_backend_common.exception.ErrorCode;
import com.example.trans_backend_file.manager.connection.ConnectionFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.DisposableBean;

public abstract class AbstractConnectionFactory implements ConnectionFactory, DisposableBean {


    public final RequestConfig requestConfig=RequestConfig.custom()
            .setSocketTimeout(5000)
            .setConnectTimeout(5000)
            .build();
    
    private volatile HttpClient httpClient;

    @Override
    public HttpClient getHttpClient() {
        if(httpClient==null){
            synchronized (this){
                if(httpClient==null){
                    httpClient = buildHttpClient();
                    return httpClient;
                }
            }
        }
        return httpClient;
    }

    public abstract HttpClient buildHttpClient();

    @Override
    public void destroy(){
        if(httpClient!=null){
            try {
                ((PoolingHttpClientConnectionManager)httpClient).close();
            }catch (Exception e){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"关闭连接池失败");
            }
        }
    }
}
