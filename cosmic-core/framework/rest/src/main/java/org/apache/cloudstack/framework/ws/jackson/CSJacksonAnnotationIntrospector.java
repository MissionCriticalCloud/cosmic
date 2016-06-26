package org.apache.cloudstack.framework.ws.jackson;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * Adds introspectors for the annotations added specifically for CloudStack
 * Web Services.
 */
public class CSJacksonAnnotationIntrospector extends NopAnnotationIntrospector {

    private static final long serialVersionUID = 5532727887216652602L;

    @Override
    public Version version() {
        return new Version(1, 7, 0, "abc", "org.apache.cloudstack", "cloudstack-framework-rest");
    }

    @Override
    public Object findSerializer(final Annotated a) {
        final AnnotatedElement ae = a.getAnnotated();
        final Url an = ae.getAnnotation(Url.class);
        if (an == null) {
            return null;
        }

        if (an.type() == String.class) {
            return new UriSerializer(an);
        } else if (an.type() == List.class) {
            return new UrisSerializer(an);
        }

        throw new UnsupportedOperationException("Unsupported type " + an.type());
    }
}
