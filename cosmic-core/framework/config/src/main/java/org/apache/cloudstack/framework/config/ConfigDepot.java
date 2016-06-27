package org.apache.cloudstack.framework.config;

import java.util.Set;

/**
 * ConfigDepot is a repository of configurations.
 */
public interface ConfigDepot {

    ConfigKey<?> get(String paramName);

    Set<ConfigKey<?>> getConfigListByScope(String scope);

    <T> void set(ConfigKey<T> key, T value);

    <T> void createOrUpdateConfigObject(String componentName, ConfigKey<T> key, String value);
}
