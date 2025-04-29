package com.example.trans_backend_gateway.limiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MyRedisLimiter {


    private final String keyNamespace="gateway:limit:";

    private final RedisTemplate<String, Long> redisTemplate;

    private final RedisScript<List<Long>> redisScript;
    /**    * @param redisScript 该RedisScript对象将会自动注入进来，该对象使用的正是上面介绍的request-rate-limiter.lua脚本    */
    @Autowired
    public MyRedisLimiter(RedisTemplate<String,Long> redisTemplate,
                              RedisScript<List<Long>> redisScript){
        this.redisTemplate = redisTemplate;
        this.redisScript = redisScript;
    }

    /**
     * 限流方法
     * @param key 限流的key
     * @param replenishRate 填充速率
     * @param burstCapacity 容量
     * @return
     */
    public boolean isAllowed(String key, int replenishRate, int burstCapacity){
        List<String> keys = Arrays.asList(keyNamespace+key+"tokens", keyNamespace+key+"timestamp");
        try {
            List<Long> response = this.redisTemplate.execute(this.redisScript, keys,
                    replenishRate+"",                    burstCapacity+"",                    Instant.now().getEpochSecond()+"",                    1+"");
            if(response.get(0) ==0){
                return false;
            }else{
                return true;
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
            return true;
        }

    }
}
