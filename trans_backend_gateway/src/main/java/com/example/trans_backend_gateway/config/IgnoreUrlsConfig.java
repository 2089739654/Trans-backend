package com.example.trans_backend_gateway.config;


import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("gateway.ignore")
public class IgnoreUrlsConfig {
    private List<String> urls;
}
