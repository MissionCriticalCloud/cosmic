package com.cloud.utils.db;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * JoinType is only used with SecondaryTable.  Temporary solution.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface JoinType {
    String type() default "inner";
}
