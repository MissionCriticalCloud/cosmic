package com.cloud.storage.image;

import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.DataStoreDriver;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.storage.Upload;

public interface ImageStoreDriver extends DataStoreDriver {
    String createEntityExtractUrl(DataStore store, String installPath, ImageFormat format, DataObject dataObject);

    void deleteEntityExtractUrl(DataStore store, String installPath, String url, Upload.Type entityType);
}
