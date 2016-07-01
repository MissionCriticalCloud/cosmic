package org.apache.cloudstack.storage.image;

import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Upload;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreDriver;

public interface ImageStoreDriver extends DataStoreDriver {
    String createEntityExtractUrl(DataStore store, String installPath, ImageFormat format, DataObject dataObject);

    void deleteEntityExtractUrl(DataStore store, String installPath, String url, Upload.Type entityType);
}
