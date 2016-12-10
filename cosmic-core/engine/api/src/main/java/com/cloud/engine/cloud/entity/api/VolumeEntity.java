package com.cloud.engine.cloud.entity.api;

import com.cloud.engine.datacenter.entity.api.StorageEntity;
import com.cloud.engine.entity.api.CloudStackEntity;
import com.cloud.engine.subsystem.api.storage.type.VolumeType;

public interface VolumeEntity extends CloudStackEntity {

    /**
     * Migrate using a reservation.
     *
     * @param reservationToken reservation token
     */
    void migrate(String reservationToken);

    /**
     * Perform the copy
     *
     * @param dest copy to this volume
     */
    void copy(VolumeEntity dest);

    /**
     * Destroy the volume
     */
    void destroy();

    long getSize();

    VolumeType getType();

    StorageEntity getDataStore();
}
