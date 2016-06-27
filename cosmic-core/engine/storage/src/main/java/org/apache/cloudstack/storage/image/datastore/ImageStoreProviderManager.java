package org.apache.cloudstack.storage.image.datastore;

import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.image.ImageStoreDriver;

import java.util.List;

public interface ImageStoreProviderManager {
    ImageStoreEntity getImageStore(long dataStoreId);

    ImageStoreEntity getImageStore(String uuid);

    List<DataStore> listImageStores();

    List<DataStore> listImageCacheStores();

    List<DataStore> listImageStoresByScope(ZoneScope scope);

    List<DataStore> listImageStoreByProvider(String provider);

    List<DataStore> listImageCacheStores(Scope scope);

    boolean registerDriver(String uuid, ImageStoreDriver driver);

    DataStore getImageStore(List<DataStore> imageStores);
}
