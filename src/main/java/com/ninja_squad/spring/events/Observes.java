package com.ninja_squad.spring.events;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be set on any singleton bean method with a single parameter, and which will be called when an
 * event that is an instance of the method parameter is fired using{@link EventFirer#fire(Object)}.
 * Such a method typically returns void, but is not forced to. If it doesn't return void and is called by firing an
 * event, the returned value is ignored.<br>
 * The annotated method can be annotated with <code>@Async</code> as any other method, in which case firing the event
 * will not block the event firing thread.<br>
 * If the annotated method throws an exception and the method is not called asynchronously, the exception won't be
 * caught and will thus have the same effect as if the method has been called directly.
 * @author JB Nizet
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Observes {
    /**
     * Tells when the annotated method should be called. By default, it's called when the event is fired
     */
    EventMoment when() default EventMoment.IN_PROGRESS;
}
