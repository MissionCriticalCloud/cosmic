package org.apache.cloudstack.framework.config;

import org.apache.cloudstack.framework.config.ConfigKey.Scope;

/**
 * This method is used by individual storage for configuration
 */
public interface ScopedConfigStorage {
    Scope getScope();

    String getConfigValue(long id, ConfigKey<?> key);
}
