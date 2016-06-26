package org.apache.cloudstack.api;

import static java.lang.annotation.ElementType.TYPE;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ResponseObject.ResponseView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface APICommand {
    Class<? extends BaseResponse> responseObject();

    String name() default "";

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
