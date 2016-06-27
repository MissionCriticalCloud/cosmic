package org.apache.cloudstack.storage.datastore.db;

import com.cloud.storage.Volume;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;

import java.util.List;

public interface VolumeDataStoreDao extends GenericDao<VolumeDataStoreVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {

    List<VolumeDataStoreVO> listByStoreId(long id);

    List<VolumeDataStoreVO> listActiveOnCache(long id);

    void deletePrimaryRecordsForStore(long id);

    VolumeDataStoreVO findByVolume(long volumeId);

    VolumeDataStoreVO findByStoreVolume(long storeId, long volumeId);

    VolumeDataStoreVO findByStoreVolume(long storeId, long volumeId, boolean lock);

    List<VolumeDataStoreVO> listDestroyed(long storeId);

    void duplicateCacheRecordsOnRegionStore(long storeId);

    List<VolumeDataStoreVO> listVolumeDownloadUrls();

    void expireDnldUrlsForZone(Long dcId);

    List<VolumeDataStoreVO> listUploadedVolumesByStoreId(long id);

    List<VolumeDataStoreVO> listByVolumeState(Volume.State... states);
}
