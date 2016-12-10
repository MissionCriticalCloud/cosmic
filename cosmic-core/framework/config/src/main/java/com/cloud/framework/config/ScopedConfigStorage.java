package com.cloud.framework.config;

import com.cloud.framework.config.ConfigKey.Scope;

/**
 * This method is used by individual storage for configuration
 */
public interface ScopedConfigStorage {
    Scope getScope();

    String getConfigValue(long id, ConfigKey<?> key);
}
