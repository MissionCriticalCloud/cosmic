package com.cloud.api;

import static java.lang.annotation.ElementType.FIELD;

import com.cloud.acl.RoleType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
public @interface Parameter {
    String name() default "";

    String description() default "";

    boolean required() default false;

    BaseCmd.CommandType type() default BaseCmd.CommandType.OBJECT;

    BaseCmd.CommandType collectionType() default BaseCmd.CommandType.OBJECT;

    Class<?>[] entityType() default Object.class;

    boolean expose() default true;

    boolean includeInApiDoc() default true;

    int length() default 255;

    String since() default "";

    RoleType[] authorized() default {};
}
