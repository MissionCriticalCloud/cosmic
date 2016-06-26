package org.apache.cloudstack.spring.lifecycle.registry;

import com.cloud.utils.component.Named;

public class RegistryUtils {

    public static String getName(final Object object) {
        if (object instanceof Named) {
            final String name = ((Named) object).getName();
            if (name != null) {
                return name;
            }
        }

        return object == null ? null : object.getClass().getSimpleName();
    }
}
