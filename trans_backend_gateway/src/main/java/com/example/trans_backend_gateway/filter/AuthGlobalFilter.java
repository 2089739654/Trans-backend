package com.example.trans_backend_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@Component
public class AuthGlobalFilter implements GlobalFilter {

    private static final String AUTHORIZATION="Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String first = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);
        return chain.filter(exchange);
    }
}
