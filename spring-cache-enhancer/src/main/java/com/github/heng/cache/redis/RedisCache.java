package com.github.heng.cache.redis;

import com.github.heng.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @author heng.ma
 */
public class RedisCache extends org.springframework.data.redis.cache.RedisCache implements Cache {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Map<Object, ReentrantLock> map = new ConcurrentHashMap<>();
    /**
     * Create a new {@link RedisCache} with the given {@link String name} and {@link RedisCacheConfiguration}, using the
     * {@link RedisCacheWriter} to execute Redis commands supporting the cache operations.
     *
     * @param name               {@link String name} for this {@link Cache}; must not be {@literal null}.
     * @param cacheWriter        {@link RedisCacheWriter} used to perform {@link RedisCache} operations by executing the
     *                           necessary Redis commands; must not be {@literal null}.
     * @param cacheConfiguration {@link RedisCacheConfiguration} applied to this {@link RedisCache} on creation; must not
     *                           be {@literal null}.
     * @throws IllegalArgumentException if either the given {@link RedisCacheWriter} or {@link RedisCacheConfiguration}
     *                                  are {@literal null} or the given {@link String} name for this {@link RedisCache} is {@literal null}.
     */
    protected RedisCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfiguration) {
        super(name, cacheWriter, cacheConfiguration);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader, Duration ttl) {
        ValueWrapper result = get(key);

        return result != null ? (T) result.get() : getSynchronized(key, valueLoader, ttl);
    }


    @Override
    public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader, Duration ttl) {
        return retrieve(key).thenCompose(wrapper -> {

            if (wrapper != null) {
                return CompletableFuture.completedFuture((T) wrapper.get());
            }

            return valueLoader.get().thenCompose(value -> {

                Object cacheValue = processAndCheckValue(value);

                byte[] binaryKey = createAndConvertCacheKey(key);
                byte[] binaryValue = serializeCacheValue(cacheValue);


                return getCacheWriter().store(getName(), binaryKey, binaryValue, ttl).thenApply(v -> value);
            });
        });
    }

    @Override
    public void put(Object key, Object value, Duration ttl) {
        Object cacheValue = processAndCheckValue(value);

        byte[] binaryKey = createAndConvertCacheKey(key);
        byte[] binaryValue = serializeCacheValue(cacheValue);


        getCacheWriter().put(getName(), binaryKey, binaryValue, ttl);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value, Duration ttl) {
        Object cacheValue = preProcessCacheValue(value);

        if (nullCacheValueIsNotAllowed(cacheValue)) {
            return get(key);
        }

        byte[] binaryKey = createAndConvertCacheKey(key);
        byte[] binaryValue = serializeCacheValue(cacheValue);
        byte[] result = getCacheWriter().putIfAbsent(getName(), binaryKey, binaryValue, ttl);

        return result != null ? new SimpleValueWrapper(fromStoreValue(deserializeCacheValue(result))) : null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSynchronized(Object key, Callable<T> valueLoader, Duration ttl) {

        ReentrantLock lock = map.compute(key, (k, v) -> v == null ? new ReentrantLock():v);
        try {
            lock.lock();
            ValueWrapper result = get(key);
            return result != null ? (T) result.get() : loadCacheValue(key, valueLoader, ttl);
        } finally {
            lock.unlock();
            map.remove(key);
        }
    }

    protected <T> T loadCacheValue(Object key, Callable<T> valueLoader, Duration ttl) {

        T value;

        try {
            value = valueLoader.call();
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }

        put(key, value, ttl);

        return value;
    }


    @Override
    protected String createCacheKey(Object key) {
        String _key =  super.createCacheKey(key);
        log.info("redis key {}", _key);
        return _key;
    }

    private byte[] createAndConvertCacheKey (Object key) {
        String cacheKey = createCacheKey(key);

        return serializeCacheKey(cacheKey);
    }

    private boolean nullCacheValueIsNotAllowed(@Nullable Object cacheValue) {
        return cacheValue == null && !isAllowNullValues();
    }

    private Object processAndCheckValue(@Nullable Object value) {

        Object cacheValue = preProcessCacheValue(value);

        if (nullCacheValueIsNotAllowed(cacheValue)) {

            String message = String.format("Cache '%s' does not allow 'null' values; Avoid storing null"
                    + " via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null'"
                    + " via RedisCacheConfiguration", getName());

            throw new IllegalArgumentException(message);
        }

        return cacheValue;
    }
}
