package com.cloud.engine.subsystem.api.storage;

import com.cloud.model.enumeration.DataStoreRole;

import java.util.List;

public interface VolumeDataFactory {
    VolumeInfo getVolume(long volumeId, DataStore store);

    VolumeInfo getVolume(DataObject volume, DataStore store);

    VolumeInfo getVolume(long volumeId, DataStoreRole storeRole);

    VolumeInfo getVolume(long volumeId);

    VolumeInfo getVolume(long volumeId, final boolean toBeMigrated);

    List<VolumeInfo> listVolumeOnCache(long volumeId);
}
