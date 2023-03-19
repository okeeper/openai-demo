package com.okeeper.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.okeeper.utils.CacheUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OpenAIEventSourceHolder {

    private static Cache<Object, Object> cache = CacheBuilder.newBuilder()
            //设置并发级别为8，并发级别是指可以同时写缓存的线程数
            .concurrencyLevel(20)
            //设置缓存容器的初始容量为10
            .initialCapacity(10)
            //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
            .maximumSize(2000000)
            //设置写缓存后n秒钟过期
            .expireAfterWrite(15, TimeUnit.MINUTES)
            //设置缓存的移除通知
            .removalListener(notification -> {
                log.info(notification.getKey() + " " + notification.getValue() + " 被移除,原因:" + notification.getCause());
            })
            .build();

    public static String register(OpenAIEventSourceAdapter openAIEventSourceAdapter) {
        String requestId = UUID.randomUUID().toString();
        cache.put(requestId, openAIEventSourceAdapter);
        return requestId;
    }

    public static void remove(String requestId) {
        cache.invalidate(requestId);
    }

    public static OpenAIEventSourceAdapter get(String requestId) {
        return (OpenAIEventSourceAdapter) cache.getIfPresent(requestId);
    }
}
