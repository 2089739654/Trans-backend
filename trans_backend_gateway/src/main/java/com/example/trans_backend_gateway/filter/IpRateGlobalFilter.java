package com.example.trans_backend_gateway.filter;


import com.example.trans_backend_gateway.config.IpRateConfig;
import com.example.trans_backend_gateway.config.RedisConfig;
import com.example.trans_backend_gateway.limiter.MyRedisLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * IP限流过滤器
 */
@Component
@ConditionalOnProperty(name = "gateway.ipRateEnable",havingValue = "true")
public class IpRateGlobalFilter implements Ordered, GlobalFilter {

    @Autowired
    private MyRedisLimiter myRedisLimiter;

    @Resource
    private IpRateConfig ipRateConfig;



    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取到调用客户端的IP地址
        String ip = exchange.getRequest().getRemoteAddress().getHostName();
        int[] rate = ipRateConfig.getByIp(ip);
        if(rate==null){
            rate=new int[]{10,100};
        }
        //如果允许同行，没有超过该ip的流量限制
        if(myRedisLimiter.isAllowed("ip:"+ip+":",
                rate[0],rate[1]
                )){
            return chain.filter(exchange);
        }else{
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
