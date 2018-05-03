package com.cloud.storage.dao;

import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.storage.VolumeHostVO;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

import java.util.List;

public interface VolumeHostDao extends GenericDao<VolumeHostVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {

    VolumeHostVO findByHostVolume(long hostId, long volumeId);

    VolumeHostVO findByVolumeId(long volumeId);

    List<VolumeHostVO> listBySecStorage(long sserverId);

    List<VolumeHostVO> listDestroyed(long hostId);

    VolumeHostVO findVolumeByZone(long zoneId, long volumeId);
}
