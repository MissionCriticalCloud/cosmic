package com.cloud.utils;

import java.util.Map;
import java.util.Properties;

public interface PropertiesPojo {
    void load(final Properties properties);

    default void load(final Map<String, Object> map) {
        final Properties properties = new Properties();
        properties.putAll(map);
        load(properties);
    }

    Map<String, Object> buildPropertiesMap();
}
