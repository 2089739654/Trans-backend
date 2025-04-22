package com.example.trans_backend_gateway.filter;

import cn.hutool.json.JSONUtil;
import com.example.trans_backend_common.constant.UserConstant;
import com.example.trans_backend_common.entity.User;
import com.example.trans_backend_gateway.config.IgnoreUrlsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.util.List;


@Component
public class AuthGlobalFilter implements GlobalFilter {



    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Value("${auth.loginUrl}")
    private String loginUrl;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(UserConstant.TOKEN);
        if(token!=null){
            User user = (User) redisTemplate.opsForValue().get(token);
            if(user!=null){
                ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().header(UserConstant.TOKEN, JSONUtil.toJsonStr(user)).build();
                exchange = exchange.mutate().request(serverHttpRequest).build();
                return chain.filter(exchange);
            }
        }
        if(checkUrl(exchange.getRequest().getURI())){
            return chain.filter(exchange);
        }
        //重定向到登录页面
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create(loginUrl));
        return exchange.getResponse().setComplete();
    }


    //检查是否是白名单
    public boolean checkUrl(URI uri){
        List<String> urls = ignoreUrlsConfig.getUrls();
        PathMatcher pathMatcher=new AntPathMatcher();
        for (String url : urls) {
            if(pathMatcher.match(url,uri.getPath())){
                return true;
            }
        }
        return false;
    }

}
