package com.okeeper.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CacheUtils {

    private static Cache<Object, Object> cache = CacheBuilder.newBuilder()
            //设置并发级别为8，并发级别是指可以同时写缓存的线程数
            .concurrencyLevel(20)
            //设置缓存容器的初始容量为10
            .initialCapacity(10)
            //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
            .maximumSize(2000000)
            //是否需要统计缓存情况,该操作消耗一定的性能,生产环境应该去除
            //.recordStats()
            //设置写缓存后n秒钟过期
            .expireAfterWrite(15, TimeUnit.HOURS)
            //设置读写缓存后n秒钟过期,实际很少用到,类似于expireAfterWrite
            //.expireAfterAccess(17, TimeUnit.SECONDS)
            //只阻塞当前数据加载线程，其他线程返回旧值
            //.refreshAfterWrite(13, TimeUnit.SECONDS)
            //设置缓存的移除通知
            .removalListener(notification -> {
                log.info(notification.getKey() + " " + notification.getValue() + " 被移除,原因:" + notification.getCause());
            })
            //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
            .build();

    public static Object cacheAndGet(Object cacheKey, Object cacheable) {
        try {
            return cache.get(cacheKey, () -> cacheable);
        } catch (ExecutionException e) {
            log.error("cache error.{} - {}", cacheKey, cacheable, e);
        }
        return cacheable;
    }

    public static void set(Object cacheKey, Object cacheable) {
        cache.put(cacheKey, cacheable);
    }

    public static Object get(Object cacheKey) {
        return cache.getIfPresent(cacheKey);
    }

    public static <T> T cacheIfNotExists(Object cacheKey, Callable<T> callable) {
        try {
            return (T) cache.get(cacheKey, callable);
        } catch (ExecutionException e) {
            log.error("cache error.{} - {}", cacheKey, e);
            throw new RuntimeException("cache error.");
        }
    }


    public static void remove(Object cacheKey) {
        try {
            cache.invalidate(cacheKey);
        } catch (Exception e) {
            log.error("invalidate cache error.{} - {}", cacheKey, e);
            throw new RuntimeException("invalidate cache error.");
        }
    }
}
