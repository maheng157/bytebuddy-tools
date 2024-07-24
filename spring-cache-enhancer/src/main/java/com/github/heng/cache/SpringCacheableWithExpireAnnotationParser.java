package com.github.heng.cache;

import org.springframework.cache.annotation.CacheAnnotationParser;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CachePutOperation;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

/**
 * @author heng.ma
 */
public class SpringCacheableWithExpireAnnotationParser implements CacheAnnotationParser {

    private final Set<Class<? extends Annotation>> cacheableClass;

    public SpringCacheableWithExpireAnnotationParser() {
        this.cacheableClass = Set.of(Cacheable.class);
    }

    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        return AnnotationUtils.isCandidateClass(targetClass, cacheableClass);
    }

    @Override
    public Collection<CacheOperation> parseCacheAnnotations(Class<?> type) {
        DefaultCacheConfig defaultConfig = new DefaultCacheConfig(type);
        return getCacheAnnotations(defaultConfig, type);
    }

    @Override
    public Collection<CacheOperation> parseCacheAnnotations(Method method) {
        DefaultCacheConfig defaultConfig = new DefaultCacheConfig(method.getDeclaringClass());
        return getCacheAnnotations(defaultConfig, method);
    }

    private Collection<CacheOperation> getCacheAnnotations (DefaultCacheConfig defaultConfig, AnnotatedElement element) {
        Set<? extends Annotation> set = AnnotatedElementUtils.getAllMergedAnnotations(element, cacheableClass);
        if (set.isEmpty()) {
            set = AnnotatedElementUtils.findAllMergedAnnotations(element, cacheableClass);
        }

        if (set.isEmpty()) {
            return null;
        }

        return set.stream().map(a -> parse(defaultConfig, element, a)).toList();
    }


    private CacheOperation parse (DefaultCacheConfig defaultConfig, AnnotatedElement ae, Annotation a) {
        if (a instanceof Cacheable cacheable) {
            return parseCacheable(defaultConfig, ae, cacheable);
        }else if (a instanceof CachePut cachePut) {
            return parsePutAnnotation(defaultConfig, ae, cachePut);
        }
        return null;
    }

    private CacheOperation parseCacheable (DefaultCacheConfig defaultConfig, AnnotatedElement ae, Cacheable cacheable) {
        CacheableExpireOperation.Builder builder = new CacheableExpireOperation.Builder();
        builder.setName(ae.toString());
        builder.setCacheNames(cacheable.cacheNames());
        builder.setCondition(cacheable.condition());
        builder.setUnless(cacheable.unless());
        builder.setKey(cacheable.key());
        builder.setKeyGenerator(cacheable.keyGenerator());
        builder.setCacheManager(cacheable.cacheManager());
        builder.setCacheResolver(cacheable.cacheResolver());
        builder.setSync(cacheable.sync());
        builder.setTtl(cacheable.ttl(), cacheable.unit());


        defaultConfig.applyDefault(builder);
        CacheableOperation op = builder.build();
        validateCacheOperation(ae, op);

        return op;
    }

    private CacheOperation parsePutAnnotation(DefaultCacheConfig defaultConfig, AnnotatedElement ae, CachePut cachePut) {

        CachePutExpireOperation.Builder builder = new CachePutExpireOperation.Builder();

        builder.setName(ae.toString());
        builder.setCacheNames(cachePut.cacheNames());
        builder.setCondition(cachePut.condition());
        builder.setUnless(cachePut.unless());
        builder.setKey(cachePut.key());
        builder.setKeyGenerator(cachePut.keyGenerator());
        builder.setCacheManager(cachePut.cacheManager());
        builder.setCacheResolver(cachePut.cacheResolver());
        builder.setTtl(cachePut.ttl(), cachePut.unit());


        defaultConfig.applyDefault(builder);
        CachePutOperation op = builder.build();
        validateCacheOperation(ae, op);

        return op;
    }

    /**
     * Validates the specified {@link CacheOperation}.
     * <p>Throws an {@link IllegalStateException} if the state of the operation is
     * invalid. As there might be multiple sources for default values, this ensures
     * that the operation is in a proper state before being returned.
     * @param ae the annotated element of the cache operation
     * @param operation the {@link CacheOperation} to validate
     */
    private void validateCacheOperation(AnnotatedElement ae, CacheOperation operation) {
        if (StringUtils.hasText(operation.getKey()) && StringUtils.hasText(operation.getKeyGenerator())) {
            throw new IllegalStateException("Invalid cache annotation configuration on '" +
                    ae.toString() + "'. Both 'key' and 'keyGenerator' attributes have been set. " +
                    "These attributes are mutually exclusive: either set the SpEL expression used to" +
                    "compute the key at runtime or set the name of the KeyGenerator bean to use.");
        }
        if (StringUtils.hasText(operation.getCacheManager()) && StringUtils.hasText(operation.getCacheResolver())) {
            throw new IllegalStateException("Invalid cache annotation configuration on '" +
                    ae.toString() + "'. Both 'cacheManager' and 'cacheResolver' attributes have been set. " +
                    "These attributes are mutually exclusive: the cache manager is used to configure a" +
                    "default cache resolver if none is set. If a cache resolver is set, the cache manager" +
                    "won't be used.");
        }
    }
    /**
     * Provides default settings for a given set of cache operations.
     */
    private static class DefaultCacheConfig {

        private final Class<?> target;

        @Nullable
        private String[] cacheNames;

        @Nullable
        private String keyGenerator;

        @Nullable
        private String cacheManager;

        @Nullable
        private String cacheResolver;

        private boolean initialized = false;

        public DefaultCacheConfig(Class<?> target) {
            this.target = target;
        }

        /**
         * Apply the defaults to the specified {@link CacheOperation.Builder}.
         * @param builder the operation builder to update
         */
        public void applyDefault(CacheOperation.Builder builder) {
            if (!this.initialized) {
                CacheConfig annotation = AnnotatedElementUtils.findMergedAnnotation(this.target, CacheConfig.class);
                if (annotation != null) {
                    this.cacheNames = annotation.cacheNames();
                    this.keyGenerator = annotation.keyGenerator();
                    this.cacheManager = annotation.cacheManager();
                    this.cacheResolver = annotation.cacheResolver();
                }
                this.initialized = true;
            }

            if (builder.getCacheNames().isEmpty() && this.cacheNames != null) {
                builder.setCacheNames(this.cacheNames);
            }
            if (!StringUtils.hasText(builder.getKey()) && !StringUtils.hasText(builder.getKeyGenerator()) &&
                    StringUtils.hasText(this.keyGenerator)) {
                builder.setKeyGenerator(this.keyGenerator);
            }

            if (StringUtils.hasText(builder.getCacheManager()) || StringUtils.hasText(builder.getCacheResolver())) {
                // One of these is set so we should not inherit anything
            }
            else if (StringUtils.hasText(this.cacheResolver)) {
                builder.setCacheResolver(this.cacheResolver);
            }
            else if (StringUtils.hasText(this.cacheManager)) {
                builder.setCacheManager(this.cacheManager);
            }
        }
    }
}
