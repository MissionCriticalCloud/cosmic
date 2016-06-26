package org.apache.cloudstack.storage.cache.manager;

import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;

public interface StorageCacheReplacementAlgorithm {
    DataObject chooseOneToBeReplaced(DataStore store);
}
