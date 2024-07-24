package com.github.heng.bytebuddy.spring.autoconfig;

import com.github.heng.bytebuddy.spring.cache.ByteBuddyCacheProxy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.cache.interceptor.CacheAspectSupport;

/**
 * @author heng.ma
 */
@AutoConfiguration
@ConditionalOnMissingBean(CachingConfigurationSelector.class)
public class SpringCacheAutoConfiguration {

    public ByteBuddyCacheProxy byteBuddyCacheProxy (CacheAspectSupport cacheAspectSupport) {
        return new ByteBuddyCacheProxy(cacheAspectSupport);
    }

}
