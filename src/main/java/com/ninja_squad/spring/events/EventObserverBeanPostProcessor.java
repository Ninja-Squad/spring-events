/*
 * The MIT License
 *
 * Copyright (c) 2014, Ninja Squad
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ninja_squad.spring.events;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Bean post processor which collects all the bean methods annotated with {@link Observes} in order to call them
 * when an event of the appropriate type is fired. This bean should be injected into beans which want to fire
 * events using its EventPublisher interface.<br>
 * Note that once the singleton beans have been instantiated, newly created beans (for example request-scoped or
 * session-scoped beans) are ignored by this post processor. So <code>@Observes</code> should only be placed on methods
 * of singleton beans.
 * @author JB Nizet
 */
public class EventObserverBeanPostProcessor
        implements BeanPostProcessor, BeanFactoryAware, SmartInitializingSingleton, EventPublisher {

    private static final ReflectionUtils.MethodFilter HAS_OBSERVES_ANNOTATION  = new ReflectionUtils.MethodFilter() {
        @Override
        public boolean matches(Method method) {
            return method.getAnnotation(Observes.class) != null;
        }
    };

    private BeanFactory beanFactory;

    private boolean configFrozen;

    /**
     * Set containing the name of all the beans containing at least one method annotated with Observed. We can't keep a
     * reference to the beans during the post-processing phase, because the beans might be proxied by another post
     * processor.
     */
    private Set<String> observerBeanNames = new HashSet<String>();

    /**
     * Map containing, for each observed type of event collected by scanning the arguments of the @Observes annotated
     * methods, the associated firers.
     */
    private Multimap<Class<?>, EventPublisher> eventFirers = ArrayListMultimap.create();

    /**
     * Cache containing, for each concrete class of fired event, the set of firers to call.
     */
    private LoadingCache<Class<?>, Set<EventPublisher>> eventClassToFirersCache =
        CacheBuilder.newBuilder().build(new EventClassToFirersCacheLoader());

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (!configFrozen) {

            // we don't directly create the event firers here, because other bean post processors, like the Async
            // post processor, might come later and proxify the bean. We want to make sure the firer calls the bean
            // through this proxy, so we keep the bean name in a set if it has an observer method, and once the
            // singletons are instantiated, we go through all the bean names and create the firers.

            final Class<?> targetClass = AopUtils.getTargetClass(bean);
            ReflectionUtils.MethodCallback methodCallback = new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (method.getParameterTypes().length != 1) {
                        throw new IllegalStateException(
                            String.format("The oberver method %s of class %s must take a single parameter",
                                          method.getName(),
                                          targetClass.getName()));
                    }
                    observerBeanNames.add(beanName);
                }
            };
            ReflectionUtils.doWithMethods(targetClass, methodCallback, HAS_OBSERVES_ANNOTATION);
        }
        return bean;
    }

    @Override
    public void afterSingletonsInstantiated() {
        configFrozen = true;

        for (String beanName : observerBeanNames) {
            final Object bean = beanFactory.getBean(beanName);
            final Class<?> targetClass = AopUtils.getTargetClass(bean);
            ReflectionUtils.MethodCallback methodCallback = new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    Observes observes = AnnotationUtils.getAnnotation(method, Observes.class);
                    processObserver(observes, method, bean, targetClass);
                }
            };
            ReflectionUtils.doWithMethods(targetClass, methodCallback, HAS_OBSERVES_ANNOTATION);
        }

        // free some memory
        observerBeanNames = null;
    }

    private void processObserver(Observes observes, Method method, Object bean, Class<?> targetClass) {
        Method actualMethod = getActualMethod(method, bean, targetClass);
        ReflectionUtils.makeAccessible(actualMethod);

        Class<?> eventType = method.getParameterTypes()[0];

        EventPublisher firer = createEventFirer(observes, bean, actualMethod);

        eventFirers.put(eventType, firer);
    }

    private Method getActualMethod(Method method, Object bean, Class<?> targetClass) {
        if (AopUtils.isJdkDynamicProxy(bean)) {
            try {
                // Found a @Observes method on the target class for this JDK proxy ->
                // is it also present on the proxy itself?
                return bean.getClass().getMethod(method.getName(), method.getParameterTypes());
            }
            catch (SecurityException ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
            catch (NoSuchMethodException ex) {
                throw new IllegalStateException(String.format(
                    "@Observes method '%s' found on bean target class '%s', " +
                        "but not found in any interface(s) for bean JDK proxy. Either " +
                        "pull the method up to an interface or switch to subclass (CGLIB) " +
                        "proxies by setting proxy-target-class/proxyTargetClass " +
                        "attribute to 'true'",
                    method.getName(),
                    targetClass.getName()));
            }
        }
        return method;
    }

    private EventPublisher createEventFirer(Observes observes, Object bean, Method actualMethod) {
        EventPhase when = observes.when();
        if (when == EventPhase.IN_PROGRESS) {
            return new InProgressEventPublisher(bean, actualMethod);
        }
        else if (when == EventPhase.AFTER_COMMIT) {
            return new AfterCommitEventPublisher(bean, actualMethod);
        }
        else if (when == EventPhase.AFTER_ROLLBACK) {
            return new AfterRollbackEventPublisher(bean, actualMethod);
        }
        else {
            throw new IllegalStateException("unhandled event moment: " + when);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void fire(Object event) {
        for (EventPublisher eventPublisher : eventClassToFirersCache.getUnchecked(event.getClass())) {
            eventPublisher.fire(event);
        }
    }

    /**
     * The cache loader of the cache of classes to event firers
     */
    private class EventClassToFirersCacheLoader extends CacheLoader<Class<?>, Set<EventPublisher>> {
        @Override
        public Set<EventPublisher> load(Class<?> key) {
            Set<Class<?>> allTypes = getAllTypes(key);
            ImmutableSet.Builder<EventPublisher> result = new ImmutableSet.Builder<EventPublisher>();
            for (Class<?> type : allTypes) {
                result.addAll(eventFirers.get(type));
            }
            return result.build();
        }

        private Set<Class<?>> getAllTypes(Class<?> type) {
            Set<Class<?>> result = new HashSet<Class<?>>();
            fillAllTypes(type, result);
            return result;
        }

        private void fillAllTypes(Class<?> type, Set<Class<?>> result) {
            result.add(type);
            for (Class<?> i : type.getInterfaces()) {
                fillAllTypes(i, result);
            }
            Class<?> superclass = type.getSuperclass();
            if (superclass != null) {
                fillAllTypes(superclass, result);
            }
        }
    }
}
