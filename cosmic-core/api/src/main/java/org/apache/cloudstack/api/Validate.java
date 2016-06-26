package org.apache.cloudstack.api;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
public @interface Validate {
    Class<?>[] validators() default Object.class;

    String description() default "";
}
