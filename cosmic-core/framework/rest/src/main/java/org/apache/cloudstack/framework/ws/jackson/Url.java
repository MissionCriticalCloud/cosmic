package org.apache.cloudstack.framework.ws.jackson;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Url can be placed onto a method to construct an URL from the returned
 * results.
 * <p>
 * This annotation is supplemental to JAX-RS 2.0's annotations.  JAX-RS 2.0
 * annotations do not include a way to construct an URL.  Of
 * course, this only works with how CloudStack works.
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME)
public @interface Url {
    /**
     * @return the class that the path should belong to.
     */
    Class<?> clazz() default Object.class;

    /**
     * @return the name of the method that the path should call back to.
     */
    String method();

    String name() default "";

    Class<?> type() default String.class;
}
