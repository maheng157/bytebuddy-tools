package com.github.heng.bytebuddy.spring;

import jakarta.annotation.Nonnull;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

/**
 * spring enhancer by bytebuddy
 * strengthen spring bean method while create spring bean by bytebuddy
 * @see BeanPostProcessor#postProcessAfterInitialization(Object, String)
 * @author heng.ma
 */
public abstract class ByteBuddyProxy implements BeanPostProcessor, ApplicationContextAware {

    private AutowireCapableBeanFactory beanFactory;
    private final Object interceptor;
    private final ByteBuddy byteBuddy;

    /**
     * @param interceptor bytebuddy interceptor
     * @see net.bytebuddy.implementation.MethodDelegation#to(Object)
     */
    public ByteBuddyProxy(Object interceptor) {
        this.interceptor = interceptor;
        this.byteBuddy = new ByteBuddy(ClassFileVersion.ofThisVm(ClassFileVersion.JAVA_V17));
    }

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        return proxy(bean, beanName);
    }

    private Object proxy (Object bean, @SuppressWarnings("unused") String beanName) {
        Class<?> clazz = bean.getClass();
        ElementMatcher<MethodDescription> elementMatcher = ElementMatchers.isAnnotatedWith(ElementMatchers.anyOf(annotationsOnMethod()));
        //judge if the class has methods to proxy
        boolean b = Arrays.stream(clazz.getDeclaredMethods()).anyMatch(m -> elementMatcher.matches(new MethodDescription.ForLoadedMethod(m)));
        if (b) {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> argsConstructor = Arrays.stream(constructors).max(Comparator.comparingInt(Constructor::getParameterCount)).orElse(constructors[0]);
            try (DynamicType.Unloaded<?> unloaded = byteBuddy.subclass(clazz)
                    .method(elementMatcher)
                    .intercept(MethodDelegation.to(interceptor))
                    .constructor(ElementMatchers.not(ElementMatchers.isDefaultConstructor()))
                    .intercept(MethodCall.invoke(argsConstructor).onSuper().withAllArguments())
                    .make()){
                return beanFactory.createBean(unloaded.load(clazz.getClassLoader()).getLoaded());
            }
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    /**
     * if method has annotations of returned, bytebuddy will strengthen the method
     * @return the annotations on method to proxy by bytebuddy
     */
    protected abstract Class<? extends Annotation>[] annotationsOnMethod ();
}
