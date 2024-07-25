package com.github.heng.cache.redis;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.util.Map;

/**
 * @author heng.ma
 */
public class RedisCacheManager extends org.springframework.data.redis.cache.RedisCacheManager {
    public RedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration, boolean allowRuntimeCacheCreation, Map<String, RedisCacheConfiguration> initialCacheConfigurations) {
        super(cacheWriter, defaultCacheConfiguration, allowRuntimeCacheCreation, initialCacheConfigurations);
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfiguration) {
        return new com.github.heng.cache.redis.RedisCache(name, getCacheWriter(),cacheConfiguration);
    }
}
