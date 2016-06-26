package com.cloud.agent.dao;

import com.cloud.utils.component.Manager;

/**
 *
 */
public interface StorageComponent extends Manager {
    String get(String key);

    void persist(String key, String value);
}
