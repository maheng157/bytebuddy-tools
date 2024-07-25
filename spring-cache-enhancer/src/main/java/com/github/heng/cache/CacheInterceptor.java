package com.github.heng.cache;

import net.bytebuddy.implementation.bind.annotation.*;
import org.springframework.cache.interceptor.CacheOperationInvoker;
import org.springframework.cache.interceptor.ExpireSupportedCacheAspect;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author heng.ma
 */
public class CacheInterceptor extends ExpireSupportedCacheAspect{

    @RuntimeType
    public Object bytebuddyInterceptor (@SuperCall Callable<?> callable, @This Object target, @Origin Method method, @AllArguments Object[] args) {
        CacheOperationInvoker invoker = () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return execute(invoker, target, method, args);
    }
}
