package com.github.heng.cache;

import org.springframework.cache.interceptor.CacheableOperation;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author heng.ma
 */
public class CacheableExpireOperation extends CacheableOperation {

    private Duration ttl;

    /**
     * Create a new {@link CacheableOperation} instance from the given builder.
     *
     * @param b
     * @since 4.3
     */
    public CacheableExpireOperation(Builder b) {
        super(b);
        this.ttl = b.ttl;
    }

    public Duration getTtl() {
        return ttl;
    }

    public static class Builder extends CacheableOperation.Builder {
        private Duration ttl;

        public void setTtl (long time, TimeUnit timeUnit) {
            this.ttl = Duration.ofMillis(timeUnit.toMillis(time));
        }

        @Override
        public CacheableOperation build() {
            return new CacheableExpireOperation(this);
        }
    }
}
