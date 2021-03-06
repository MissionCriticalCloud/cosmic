package com.cloud.storage.datastore;

import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.DataStoreManager;
import com.cloud.engine.subsystem.api.storage.Scope;
import com.cloud.engine.subsystem.api.storage.ZoneScope;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.model.enumeration.DataStoreRole;
import com.cloud.storage.ScopeType;
import com.cloud.storage.image.datastore.ImageStoreProviderManager;

import javax.inject.Inject;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class DataStoreManagerImpl implements DataStoreManager {
    @Inject
    PrimaryDataStoreProviderManager primaryStoreMgr;
    @Inject
    ImageStoreProviderManager imageDataStoreMgr;

    @Override
    public DataStore getDataStore(final long storeId, final DataStoreRole role) {
        try {
            if (role == DataStoreRole.Primary) {
                return primaryStoreMgr.getPrimaryDataStore(storeId);
            } else if (role == DataStoreRole.Image) {
                return imageDataStoreMgr.getImageStore(storeId);
            } else if (role == DataStoreRole.ImageCache) {
                return imageDataStoreMgr.getImageStore(storeId);
            }
        } catch (final CloudRuntimeException e) {
            throw e;
        }
        throw new CloudRuntimeException("un recognized type" + role);
    }

    @Override
    public DataStore getPrimaryDataStore(final long storeId) {
        return primaryStoreMgr.getPrimaryDataStore(storeId);
    }

    @Override
    public DataStore getPrimaryDataStore(final String storeUuid) {
        return primaryStoreMgr.getPrimaryDataStore(storeUuid);
    }

    @Override
    public DataStore getDataStore(final String uuid, final DataStoreRole role) {
        if (role == DataStoreRole.Primary) {
            return primaryStoreMgr.getPrimaryDataStore(uuid);
        } else if (role == DataStoreRole.Image) {
            return imageDataStoreMgr.getImageStore(uuid);
        }
        throw new CloudRuntimeException("un recognized type" + role);
    }

    @Override
    public List<DataStore> getImageStoresByScope(final ZoneScope scope) {
        return imageDataStoreMgr.listImageStoresByScope(scope);
    }

    @Override
    public DataStore getImageStore(final long zoneId) {
        final List<DataStore> stores = getImageStoresByScope(new ZoneScope(zoneId));
        if (stores == null || stores.size() == 0) {
            return null;
        }
        return imageDataStoreMgr.getImageStore(stores);
    }

    @Override
    public List<DataStore> getImageCacheStores(final Scope scope) {
        return imageDataStoreMgr.listImageCacheStores(scope);
    }

    @Override
    public List<DataStore> listImageStores() {
        return imageDataStoreMgr.listImageStores();
    }

    @Override
    public List<DataStore> listImageCacheStores() {
        return imageDataStoreMgr.listImageCacheStores();
    }

    @Override
    public boolean isRegionStore(final DataStore store) {
        return store.getScope().getScopeType() == ScopeType.ZONE && store.getScope().getScopeId() == null;
    }

    public void setPrimaryStoreMgr(final PrimaryDataStoreProviderManager primaryStoreMgr) {
        this.primaryStoreMgr = primaryStoreMgr;
    }

    public void setImageDataStoreMgr(final ImageStoreProviderManager imageDataStoreMgr) {
        this.imageDataStoreMgr = imageDataStoreMgr;
    }
}
