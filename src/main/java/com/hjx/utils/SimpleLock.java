package com.hjx.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SimpleLock {

    public static Cache<String, Object> cacheMap = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .maximumSize(Integer.MAX_VALUE)
            .build();


    public static boolean lock(String key) {
        if (Objects.nonNull(cacheMap.getIfPresent(key))) {
            return true;
        }
        cacheMap.put(key, "1");
        return false;
    }

    public static void release(String key) {
        cacheMap.invalidate(key);
    }

}
