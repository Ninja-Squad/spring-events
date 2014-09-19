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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be set on any singleton bean method with a single parameter, and which will be called when an
 * event that is an instance of the method parameter is fired using {@link EventPublisher#fire(Object)}.
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
    EventPhase when() default EventPhase.IN_PROGRESS;
}
