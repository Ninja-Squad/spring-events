package com.ninja_squad.spring.events;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Event firer which calls an observing method immediately
 * @author JB Nizet
 */
class InProgressEventFirer implements EventFirer {

    private final Object bean;
    private final Method method;

    public InProgressEventFirer(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    @Override
    public void fire(Object event) {
        ReflectionUtils.invokeMethod(method, bean, event);
    }
}
