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

    @Nullable
    <T> T get(Object key, Callable<T> valueLoader, @Nullable Duration ttl);

    <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader, @Nullable Duration ttl);

    void put(Object key, @Nullable Object value, @Nullable Duration ttl);

    default ValueWrapper putIfAbsent(Object key, @Nullable Object value, @Nullable Duration ttl) {
        ValueWrapper existingValue = get(key);
        if (existingValue == null) {
            put(key, value, ttl);
        }
        return existingValue;
    }
}
