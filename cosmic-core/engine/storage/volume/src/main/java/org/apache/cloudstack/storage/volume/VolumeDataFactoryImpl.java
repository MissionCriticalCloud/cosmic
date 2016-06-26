package org.apache.cloudstack.storage.volume;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreVO;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class VolumeDataFactoryImpl implements VolumeDataFactory {
    @Inject
    VolumeDao volumeDao;
    @Inject
    VolumeDataStoreDao volumeStoreDao;
    @Inject
    DataStoreManager storeMgr;

    @Override
    public VolumeInfo getVolume(final long volumeId, final DataStore store) {
        final VolumeVO volumeVO = volumeDao.findById(volumeId);

        final VolumeObject vol = VolumeObject.getVolumeObject(store, volumeVO);

        return vol;
    }

    @Override
    public VolumeInfo getVolume(final long volumeId, final DataStoreRole storeRole) {
        final VolumeVO volumeVO = volumeDao.findById(volumeId);
        VolumeObject vol = null;
        if (storeRole == DataStoreRole.Image) {
            final VolumeDataStoreVO volumeStore = volumeStoreDao.findByVolume(volumeId);
            if (volumeStore != null) {
                final DataStore store = storeMgr.getDataStore(volumeStore.getDataStoreId(), DataStoreRole.Image);
                vol = VolumeObject.getVolumeObject(store, volumeVO);
            }
        } else {
            // Primary data store
            if (volumeVO.getPoolId() != null) {
                final DataStore store = storeMgr.getDataStore(volumeVO.getPoolId(), DataStoreRole.Primary);
                vol = VolumeObject.getVolumeObject(store, volumeVO);
            }
        }
        return vol;
    }

    @Override
    public VolumeInfo getVolume(final long volumeId) {
        final VolumeVO volumeVO = volumeDao.findByIdIncludingRemoved(volumeId);
        if (volumeVO == null) {
            return null;
        }
        VolumeObject vol = null;
        if (volumeVO.getPoolId() == null) {
            DataStore store = null;
            final VolumeDataStoreVO volumeStore = volumeStoreDao.findByVolume(volumeId);
            if (volumeStore != null) {
                store = storeMgr.getDataStore(volumeStore.getDataStoreId(), DataStoreRole.Image);
            }
            vol = VolumeObject.getVolumeObject(store, volumeVO);
        } else {
            final DataStore store = storeMgr.getDataStore(volumeVO.getPoolId(), DataStoreRole.Primary);
            vol = VolumeObject.getVolumeObject(store, volumeVO);
        }
        return vol;
    }

    @Override
    public VolumeInfo getVolume(final DataObject volume, final DataStore store) {
        final VolumeInfo vol = getVolume(volume.getId(), store);
        vol.addPayload(((VolumeInfo) volume).getpayload());
        return vol;
    }

    @Override
    public List<VolumeInfo> listVolumeOnCache(final long volumeId) {
        final List<VolumeInfo> cacheVols = new ArrayList<>();
        // find all image cache stores for this zone scope
        final List<DataStore> cacheStores = storeMgr.listImageCacheStores();
        if (cacheStores == null || cacheStores.size() == 0) {
            return cacheVols;
        }
        for (final DataStore store : cacheStores) {
            // check if the volume is stored there
            final VolumeDataStoreVO volStore = volumeStoreDao.findByStoreVolume(store.getId(), volumeId);
            if (volStore != null) {
                final VolumeInfo vol = getVolume(volumeId, store);
                cacheVols.add(vol);
            }
        }
        return cacheVols;
    }
}
