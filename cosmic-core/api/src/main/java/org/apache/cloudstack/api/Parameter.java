package org.apache.cloudstack.api;

import static java.lang.annotation.ElementType.FIELD;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.BaseCmd.CommandType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
public @interface Parameter {
    String name() default "";

    String description() default "";

    boolean required() default false;

    CommandType type() default CommandType.OBJECT;

    CommandType collectionType() default CommandType.OBJECT;

    Class<?>[] entityType() default Object.class;

    boolean expose() default true;

    boolean includeInApiDoc() default true;

    int length() default 255;

    String since() default "";

    RoleType[] authorized() default {};
}
