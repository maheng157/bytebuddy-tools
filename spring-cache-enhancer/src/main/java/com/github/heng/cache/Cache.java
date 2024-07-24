package com.github.heng.cache;

import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * extends {@link org.springframework.cache.Cache} with ttl
 * @author heng.ma
 */
public interface Cache extends org.springframework.cache.Cache {

    /**
     * get key with valueLoader and ttl
     * @param key   cache key
     * @param valueLoader fetch value strategy if no value in cache
     * @param ttl   ttl
     * @return
     * @param <T>
     */
    @Nullable
    <T> T get(Object key, Callable<T> valueLoader, Duration ttl);

    <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader, Duration ttl);

    void put(Object key, @Nullable Object value, Duration ttl);

    @Nullable
    default ValueWrapper putIfAbsent(Object key, @Nullable Object value, Duration ttl) {
        ValueWrapper existingValue = get(key);
        if (existingValue == null) {
            put(key, value, ttl);
        }
        return existingValue;
    }
}
