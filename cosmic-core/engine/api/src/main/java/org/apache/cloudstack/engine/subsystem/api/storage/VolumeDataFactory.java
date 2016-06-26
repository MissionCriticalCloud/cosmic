package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.DataStoreRole;

import java.util.List;

public interface VolumeDataFactory {
    VolumeInfo getVolume(long volumeId, DataStore store);

    VolumeInfo getVolume(DataObject volume, DataStore store);

    VolumeInfo getVolume(long volumeId, DataStoreRole storeRole);

    VolumeInfo getVolume(long volumeId);

    List<VolumeInfo> listVolumeOnCache(long volumeId);
}
