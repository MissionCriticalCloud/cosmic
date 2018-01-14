package com.cloud.api;

import static java.lang.annotation.ElementType.TYPE;

import com.cloud.acl.RoleType;
import com.cloud.api.ResponseObject.ResponseView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface APICommand {
    Class<? extends BaseResponse> responseObject();

    String name();

    APICommandGroup group();

    String description() default "";

    String usage() default "";

    boolean includeInApiDoc() default true;

    String since() default "";

    ResponseView responseView() default ResponseView.Full;

    boolean requestHasSensitiveInfo() default true;

    boolean responseHasSensitiveInfo() default true;

    RoleType[] authorized() default {};

    Class<?>[] entityType() default {};
}
