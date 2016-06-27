package org.apache.cloudstack.storage.datastore.manager;

import com.cloud.storage.StorageManager;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProvider;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreProviderManager;
import org.apache.cloudstack.engine.subsystem.api.storage.HypervisorHostListener;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import org.apache.cloudstack.storage.datastore.PrimaryDataStoreImpl;
import org.apache.cloudstack.storage.datastore.PrimaryDataStoreProviderManager;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PrimaryDataStoreProviderManagerImpl implements PrimaryDataStoreProviderManager {
    @Inject
    DataStoreProviderManager providerManager;
    @Inject
    PrimaryDataStoreDao dataStoreDao;
    Map<String, PrimaryDataStoreDriver> driverMaps;
    @Inject
    StorageManager storageMgr;

    @PostConstruct
    public void config() {
        driverMaps = new HashMap<>();
    }

    @Override
    public PrimaryDataStore getPrimaryDataStore(final long dataStoreId) {
        final StoragePoolVO dataStoreVO = dataStoreDao.findById(dataStoreId);
        if (dataStoreVO == null) {
            throw new CloudRuntimeException("Unable to locate datastore with id " + dataStoreId);
        }
        final String providerName = dataStoreVO.getStorageProviderName();
        final DataStoreProvider provider = providerManager.getDataStoreProvider(providerName);
        final PrimaryDataStoreImpl dataStore = PrimaryDataStoreImpl.createDataStore(dataStoreVO, driverMaps.get(provider.getName()), provider);
        return dataStore;
    }

    @Override
    public boolean registerDriver(final String providerName, final PrimaryDataStoreDriver driver) {
        if (driverMaps.get(providerName) != null) {
            return false;
        }
        driverMaps.put(providerName, driver);
        return true;
    }

    @Override
    public PrimaryDataStore getPrimaryDataStore(final String uuid) {
        final StoragePoolVO dataStoreVO = dataStoreDao.findByUuid(uuid);
        return getPrimaryDataStore(dataStoreVO.getId());
    }

    @Override
    public boolean registerHostListener(final String providerName, final HypervisorHostListener listener) {
        return storageMgr.registerHostListener(providerName, listener);
    }
}
