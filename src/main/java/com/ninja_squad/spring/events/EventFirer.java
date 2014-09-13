package com.ninja_squad.spring.events;

/**
 * Allows firing an event that will be received by event observing methods, i.e. methods annotated with
 * {@link Observes}. Note that a bean of type EventObserverBeanPostProcessor, which implements this interface,
 * must be added to the application context for this mechanism to work. This is best done by using the
 * {@link EnableEvents} annotation.
 * @author JB Nizet
 */
public interface EventFirer {
    void fire(Object event);
}
