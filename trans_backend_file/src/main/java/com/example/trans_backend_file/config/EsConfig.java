package com.example.trans_backend_file.config;

import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "elasticsearch")
public class EsConfig {

    private String host;

    private int port;

    private String scheme;

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public RestHighLevelClient client(){
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, scheme)));
    }

}
