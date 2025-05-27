package com.example.trans_backend_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        // 创建 CORS 配置
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOriginPattern("*"); // 允许所有域名进行跨域调用
        corsConfig.addAllowedHeader("*");       // 允许任何请求头
        corsConfig.addAllowedMethod("*");       // 允许任何方法（POST、GET等）
        corsConfig.setAllowCredentials(true);   // 允许携带凭证

        // 使用提供的 UrlBasedCorsConfigurationSource 实现 CorsConfigurationSource 接口
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // 对所有接口都有效

        return new CorsWebFilter(source);
    }

    // 可选：处理预检请求的过滤器（某些场景下需要）
//    @Bean
//    public WebFilter preFlightRequestFilter() {
//        return (ServerWebExchange exchange, WebFilterChain chain) -> {
//            if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethodValue())) {
//                return Mono.just(exchange.getResponse())
//                        .flatMap(response -> {
//                            response.getHeaders().add("Access-Control-Allow-Origin", "*");
//                            response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//                            response.getHeaders().add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
//                            response.getHeaders().add("Access-Control-Max-Age", "3600");
//                            response.setStatusCode(org.springframework.http.HttpStatus.NO_CONTENT);
//                            return Mono.empty();
//                        });
//            }
//            return chain.filter(exchange);
//        };
//    }
}
