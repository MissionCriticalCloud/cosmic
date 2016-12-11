package com.cloud.storage.image.datastore;

import com.cloud.engine.subsystem.api.storage.DataStore;

public interface ImageStoreInfo extends DataStore {
    long getImageStoreId();

    String getType();
}
