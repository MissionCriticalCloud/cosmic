package org.apache.cloudstack.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* There is a one on one mapping between the EntityReference and the EntityResponse
 * to the OTW layer. Value is the actual entity class it refers to.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityReference {
    Class[] value() default {};
}
