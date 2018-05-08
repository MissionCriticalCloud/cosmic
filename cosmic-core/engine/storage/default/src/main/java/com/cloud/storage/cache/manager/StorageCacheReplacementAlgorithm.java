package com.cloud.storage.cache.manager;

import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;

public interface StorageCacheReplacementAlgorithm {
    DataObject chooseOneToBeReplaced(DataStore store);
}
