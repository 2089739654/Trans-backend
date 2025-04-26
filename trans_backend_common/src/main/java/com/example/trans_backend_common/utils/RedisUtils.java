package com.example.trans_backend_common.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.trans_backend_common.constant.RedisConstants;
import com.example.trans_backend_common.enums.RedisValueCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class RedisUtils {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * redis存储进行统一API处理，对象序列化，TTL过期
     * @param key
     * @param value
     * @param ms
     * @param redisValueCategory
     * @return
     */
    public boolean set(String key, Object value, Long ms, RedisValueCategory redisValueCategory) {
        try {
            switch (redisValueCategory) {
                case STRING:
                    stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(value), ms, TimeUnit.MILLISECONDS);
                    break;
                case HASH:
                    Map<String, Object> map = BeanUtil.beanToMap(value, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
                    stringRedisTemplate.opsForHash().putAll(key, map);
                    stringRedisTemplate.expire(key, ms, TimeUnit.MILLISECONDS);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("redis设置错误");
        }
        return true;
    }


    /**
     *
     * @param key
     * @param a
     * @param category
     * @return
     * @param <T>
     */
    public <T> T get(String key,Class<T> a,RedisValueCategory category) {
        try {
            switch (category) {
                case STRING:
                    return JSON.parseObject(stringRedisTemplate.opsForValue().get(key),a);
                case HASH:
                    return JSON.parseObject(JSONObject.toJSONString(stringRedisTemplate.opsForHash().entries(key)),a);//TODO 可以使用反射方式进行改进，提升map转对象的效率
            }
        } catch (Exception e) {
            throw new RuntimeException("redis取值错误");
        }
        return null;
    }
    /**
     * 设置String类型数据
     * @param key
     * @param value
     * @param ms
     * @return
     */
    public Boolean set(String key, String value, Long ms) {
        try {
            stringRedisTemplate.opsForValue().set(key, value,ms,TimeUnit.MILLISECONDS);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 获取String类型数据
     * @param key
     * @return
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除对应键
     * @param key
     * @return
     */
    public Boolean delete(String key) {
        try{
            stringRedisTemplate.delete(key);
            return true;
        }catch ( Exception e){
            return false;
        }
    }

//    /**
//     * 设置Hash类型数据
//     * @param key
//     * @param hashMap
//     * @param ms
//     * @return
//     */
//    public Boolean setHash(String key, HashMap<String,Object> hashMap, Long ms) {
//        try {
//            stringRedisTemplate.opsForHash().putAll(key, hashMap);
//            stringRedisTemplate.expire(key,ms, TimeUnit.MILLISECONDS);
//            return true;
//        }catch (Exception e){
//            return false;
//        }
//    }

//    /**
//     * 获取Hash类型数据
//     * @param key
//     * @return
//     */
//     public Map<Object, Object> getHash(String key) {
//        return stringRedisTemplate.opsForHash().entries(key);
//     }

    /**
     * 获取锁，true则允许当前线程进行重建，false则不允许
     * @param id
     * @return
     */
     public boolean getMutex(Long id) {
         return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(RedisConstants.LOCK_SHOP_KEY + id, "1", RedisConstants.TEN_SECONDS, TimeUnit.MILLISECONDS));
     }

    /**
     * 无过期存数据
     * @param key
     * @param value
     * @return
     */
     public Boolean setNoExpire(String key, String value) {
         try {
             stringRedisTemplate.opsForValue().set(key, value);
         }catch (Exception e){
             return false;
         }
         return true;
     }

    /**
     * redis全局ID构造器，使用自增功能完成
     * @param key
     * @return
     */
     public Long increase(String key) {
         return stringRedisTemplate.opsForValue().increment(key, 1);
     }
}
