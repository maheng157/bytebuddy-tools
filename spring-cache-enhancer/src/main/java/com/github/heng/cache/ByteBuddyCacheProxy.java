package com.github.heng.cache;

import com.github.heng.bytebuddy.spring.ByteBuddyProxy;
import net.bytebuddy.implementation.MethodDelegation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.interceptor.CacheAspectSupport;

import java.lang.annotation.Annotation;

/**
 * strengthen {@link org.springframework.cache.interceptor.CacheAspectSupport}
 * to support class inner invoke cacheable method
 * @author heng.ma
 */
public class ByteBuddyCacheProxy extends ByteBuddyProxy {
    /**
     * @param interceptor bytebuddy interceptor
     * @see MethodDelegation#to(Object)
     */
    public ByteBuddyCacheProxy(CacheAspectSupport interceptor) {
        super(interceptor);
    }

    @Override
    protected Class<? extends Annotation>[] annotationsOnMethod() {
        return new Class[]{Caching.class, Cacheable.class, CachePut.class, CacheEvict.class};
    }
}
