package com.github.heng.cache;

import net.bytebuddy.implementation.bind.annotation.*;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author heng.ma
 */
public class CacheInterceptor extends CacheAspectSupport {

    @RuntimeType
    public Object  bytebuddyInterceptor (@SuperCall Callable<?> callable, @This Object target, @Origin Method method, @AllArguments Object[] args) {
        CacheOperationInvoker invoker = () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        CacheOperationSource cacheOperationSource = getCacheOperationSource();
        cacheOperationSource.getCacheOperations(method, AopProxyUtils.ultimateTargetClass(target));
        return execute(invoker, target, method, args);
    }
}
