package com.github.heng.cache;

import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.annotation.CacheAnnotationParser;
import org.springframework.cache.annotation.SpringCacheAnnotationParser;

/**
 * Allow customer CacheOperationParser
 * @see AnnotationCacheOperationSource
 * @author heng.ma
 */
public class AnnotationWithExpireCacheOperationSource extends AnnotationCacheOperationSource {
    private final boolean allowPublicMethodOnly;

    public AnnotationWithExpireCacheOperationSource(boolean allowPublicMethodOnly, CacheAnnotationParser ... parsers) {
        super(parsers);
        this.allowPublicMethodOnly = allowPublicMethodOnly;
    }

    /**
     * @param allowPublicMethodOnly if proxy public method only
     */
    public AnnotationWithExpireCacheOperationSource(boolean allowPublicMethodOnly) {
        this(allowPublicMethodOnly, new SpringCacheAnnotationParser(), new SpringCacheableWithExpireAnnotationParser());
    }

    @Override
    protected boolean allowPublicMethodsOnly() {
        return allowPublicMethodOnly;
    }
}
