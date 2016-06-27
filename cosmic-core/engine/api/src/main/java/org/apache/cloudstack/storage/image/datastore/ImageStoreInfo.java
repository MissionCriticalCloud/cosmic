package org.apache.cloudstack.storage.image.datastore;

import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;

public interface ImageStoreInfo extends DataStore {
    long getImageStoreId();

    String getType();
}
