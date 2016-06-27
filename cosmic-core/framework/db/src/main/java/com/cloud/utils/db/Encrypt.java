package com.cloud.utils.db;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Encrypt is a replacement for the column modification.
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Encrypt {
    boolean encrypt() default true;
}
