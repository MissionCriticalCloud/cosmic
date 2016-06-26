package org.apache.cloudstack.api;

import java.lang.reflect.Field;

public class ApiCmdTestUtil {
    public static void set(final BaseCmd cmd, final String fieldName, final Object value)
            throws IllegalArgumentException, IllegalAccessException {
        for (final Field field : cmd.getClass().getDeclaredFields()) {
            final Parameter parameter = field.getAnnotation(Parameter.class);
            if (parameter != null && fieldName.equals(parameter.name())) {
                field.setAccessible(true);
                field.set(cmd, value);
            }
        }
    }
}
