package com.github.heng.cache.caffeine;

import com.github.heng.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * @author heng.ma
 */
public class CaffeineCache extends AbstractValueAdaptingCache implements Cache {


    /**
     * Create an {@code AbstractValueAdaptingCache} with the given setting.
     *
     * @param allowNullValues whether to allow for {@code null} values
     */
    protected CaffeineCache(boolean allowNullValues) {
        super(allowNullValues);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader, Duration ttl) {
        return null;
    }

    @Override
    public <T> CompletableFuture<T> retrieve(Object key, Supplier<CompletableFuture<T>> valueLoader, Duration ttl) {
        return null;
    }

    @Override
    public void put(Object key, Object value, Duration ttl) {

    }

    @Override
    protected Object lookup(Object key) {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }
}
