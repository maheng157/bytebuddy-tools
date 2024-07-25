package com.github.heng.cache;


import java.util.Collection;

/**
 * @author heng.ma
 */
public interface CacheManager extends org.springframework.cache.CacheManager {

    Cache getCache(String name);

    Collection<String> getCacheNames();
}
