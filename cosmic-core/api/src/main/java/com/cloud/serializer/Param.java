package com.cloud.serializer;

import org.apache.cloudstack.acl.RoleType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String name() default "";

    String propName() default "";

    String description() default "";

    // 2 parameters below are used by cloudstack api
    Class<?> responseObject() default Object.class;

    boolean includeInApiDoc() default true;

    String since() default "";

    RoleType[] authorized() default {};

    boolean isSensitive() default false;
}
