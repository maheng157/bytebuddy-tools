package com.github.heng.cache.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;

/**
 * @author heng.ma
 */
public class CaffeineCacheManager extends org.springframework.cache.caffeine.CaffeineCacheManager {
    @Override
    protected Cache<Object, Object> createNativeCaffeineCache(String name) {
        return super.createNativeCaffeineCache(name);
    }

    @Override
    protected AsyncCache<Object, Object> createAsyncCaffeineCache(String name) {
        return super.createAsyncCaffeineCache(name);
    }
}
