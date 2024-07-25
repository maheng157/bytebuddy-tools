package com.github.heng.cache;

import org.springframework.cache.interceptor.CachePutOperation;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author heng.ma
 */
public class CachePutExpireOperation extends CachePutOperation {
    private final Duration ttl;
    /**
     * Create a new {@link CachePutOperation} instance from the given builder.
     *
     * @param b
     * @since 4.3
     */
    public CachePutExpireOperation(Builder b) {
        super(b);
        this.ttl = b.ttl;
    }

    public Duration getTtl() {
        return ttl;
    }

    public static class Builder extends CachePutOperation.Builder {

        private Duration ttl;

        public void setTtl (long time, TimeUnit timeUnit) {
            this.ttl = Duration.ofMillis(timeUnit.toMillis(time));
        }

        @Override
        public CachePutOperation build() {
            return new CachePutExpireOperation(this);
        }
    }
}
