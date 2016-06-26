package org.apache.cloudstack.api;

import static java.lang.annotation.ElementType.FIELD;

import org.apache.cloudstack.acl.SecurityChecker.AccessType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
public @interface ACL {

    AccessType accessType() default AccessType.UseEntry;

    String pointerToEntity() default "";

    boolean checkKeyAccess() default false;

    boolean checkValueAccess() default false;
}
