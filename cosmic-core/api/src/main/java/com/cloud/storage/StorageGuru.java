package com.cloud.storage;

import com.cloud.utils.component.Adapter;

/**
 * StorageGuru understands about how to implement different
 * types of storage pools.
 */
public interface StorageGuru extends Adapter {
    void createVolume();

    void prepareVolume();

    void destroyVolume();
}
