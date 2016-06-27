package org.apache.cloudstack.storage.image.manager;

import com.cloud.server.StatsCollector;
import com.cloud.storage.ScopeType;
import com.cloud.storage.dao.VMTemplateDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProviderManager;
import org.apache.cloudstack.engine.subsystem.api.storage.ImageStoreProvider;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.datastore.db.ImageStoreDao;
import org.apache.cloudstack.storage.datastore.db.ImageStoreVO;
import org.apache.cloudstack.storage.image.ImageStoreDriver;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;
import org.apache.cloudstack.storage.image.datastore.ImageStoreProviderManager;
import org.apache.cloudstack.storage.image.store.ImageStoreImpl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageStoreProviderManagerImpl implements ImageStoreProviderManager {
    private static final Logger s_logger = LoggerFactory.getLogger(ImageStoreProviderManagerImpl.class);
    @Inject
    ImageStoreDao dataStoreDao;
    @Inject
    VMTemplateDao imageDataDao;
    @Inject
    DataStoreProviderManager providerManager;
    @Inject
    StatsCollector _statsCollector;
    Map<String, ImageStoreDriver> driverMaps;

    @PostConstruct
    public void config() {
        driverMaps = new HashMap<>();
    }

    @Override
    public ImageStoreEntity getImageStore(final long dataStoreId) {
        final ImageStoreVO dataStore = dataStoreDao.findById(dataStoreId);
        final String providerName = dataStore.getProviderName();
        final ImageStoreProvider provider = (ImageStoreProvider) providerManager.getDataStoreProvider(providerName);
        final ImageStoreEntity imgStore = ImageStoreImpl.getDataStore(dataStore, driverMaps.get(provider.getName()), provider);
        return imgStore;
    }

    @Override
    public boolean registerDriver(final String providerName, final ImageStoreDriver driver) {
        if (driverMaps.containsKey(providerName)) {
            return false;
        }
        driverMaps.put(providerName, driver);
        return true;
    }

    @Override
    public ImageStoreEntity getImageStore(final String uuid) {
        final ImageStoreVO dataStore = dataStoreDao.findByUuid(uuid);
        return getImageStore(dataStore.getId());
    }

    @Override
    public List<DataStore> listImageStores() {
        final List<ImageStoreVO> stores = dataStoreDao.listImageStores();
        final List<DataStore> imageStores = new ArrayList<>();
        for (final ImageStoreVO store : stores) {
            imageStores.add(getImageStore(store.getId()));
        }
        return imageStores;
    }

    @Override
    public List<DataStore> listImageCacheStores() {
        final List<ImageStoreVO> stores = dataStoreDao.listImageCacheStores();
        final List<DataStore> imageStores = new ArrayList<>();
        for (final ImageStoreVO store : stores) {
            imageStores.add(getImageStore(store.getId()));
        }
        return imageStores;
    }

    @Override
    public List<DataStore> listImageStoresByScope(final ZoneScope scope) {
        final List<ImageStoreVO> stores = dataStoreDao.findByScope(scope);
        final List<DataStore> imageStores = new ArrayList<>();
        for (final ImageStoreVO store : stores) {
            imageStores.add(getImageStore(store.getId()));
        }
        return imageStores;
    }

    @Override
    public List<DataStore> listImageStoreByProvider(final String provider) {
        final List<ImageStoreVO> stores = dataStoreDao.findByProvider(provider);
        final List<DataStore> imageStores = new ArrayList<>();
        for (final ImageStoreVO store : stores) {
            imageStores.add(getImageStore(store.getId()));
        }
        return imageStores;
    }

    @Override
    public List<DataStore> listImageCacheStores(final Scope scope) {
        if (scope.getScopeType() != ScopeType.ZONE) {
            s_logger.debug("only support zone wide image cache stores");
            return null;
        }
        final List<ImageStoreVO> stores = dataStoreDao.findImageCacheByScope(new ZoneScope(scope.getScopeId()));
        final List<DataStore> imageStores = new ArrayList<>();
        for (final ImageStoreVO store : stores) {
            imageStores.add(getImageStore(store.getId()));
        }
        return imageStores;
    }

    @Override
    public DataStore getImageStore(final List<DataStore> imageStores) {
        if (imageStores.size() > 1) {
            Collections.shuffle(imageStores); // Randomize image store list.
            final Iterator<DataStore> i = imageStores.iterator();
            DataStore imageStore = null;
            while (i.hasNext()) {
                imageStore = i.next();
                // Return image store if used percentage is less then threshold value i.e. 90%.
                if (_statsCollector.imageStoreHasEnoughCapacity(imageStore)) {
                    return imageStore;
                }
            }
        }
        return imageStores.get(0);
    }
}
