package com.ninja_squad.spring.events;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to enable the bean post processing necessary to support events. It adds a bean of type
 * {@link EventFirer} to the configuration, that can be autowired everywhere an event needs to be fired.
 * @author JB Nizet
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EventConfiguration.class)
public @interface EnableEvents {
}
