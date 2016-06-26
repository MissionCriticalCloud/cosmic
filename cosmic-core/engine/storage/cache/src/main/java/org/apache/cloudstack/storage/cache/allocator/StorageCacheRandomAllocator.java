package org.apache.cloudstack.storage.cache.allocator;

import com.cloud.server.StatsCollector;
import com.cloud.storage.ScopeType;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.storage.datastore.ObjectInDataStoreManager;
import org.apache.cloudstack.storage.image.datastore.ImageStoreProviderManager;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StorageCacheRandomAllocator implements StorageCacheAllocator {
    private static final Logger s_logger = LoggerFactory.getLogger(StorageCacheRandomAllocator.class);
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    ObjectInDataStoreManager objectInStoreMgr;
    @Inject
    ImageStoreProviderManager imageStoreMgr;
    @Inject
    StatsCollector statsCollector;

    @Override
    public DataStore getCacheStore(final Scope scope) {
        if (scope.getScopeType() != ScopeType.ZONE) {
            s_logger.debug("Can only support zone wide cache storage");
            return null;
        }

        final List<DataStore> cacheStores = dataStoreMgr.getImageCacheStores(scope);
        if ((cacheStores == null) || (cacheStores.size() <= 0)) {
            s_logger.debug("Can't find staging storage in zone: " + scope.getScopeId());
            return null;
        }

        return imageStoreMgr.getImageStore(cacheStores);
    }

    @Override
    public DataStore getCacheStore(final DataObject data, final Scope scope) {
        if (scope.getScopeType() != ScopeType.ZONE) {
            s_logger.debug("Can only support zone wide cache storage");
            return null;
        }

        final List<DataStore> cacheStores = dataStoreMgr.getImageCacheStores(scope);
        if (cacheStores.size() <= 0) {
            s_logger.debug("Can't find staging storage in zone: " + scope.getScopeId());
            return null;
        }

        // if there are multiple cache stores, we give priority to the one where data is already there
        if (cacheStores.size() > 1) {
            for (final DataStore store : cacheStores) {
                final DataObjectInStore obj = objectInStoreMgr.findObject(data, store);
                if (obj != null && obj.getState() == ObjectInDataStoreStateMachine.State.Ready && statsCollector.imageStoreHasEnoughCapacity(store)) {
                    s_logger.debug("pick the cache store " + store.getId() + " where data is already there");
                    return store;
                }
            }
        }
        return imageStoreMgr.getImageStore(cacheStores);
    }
}
