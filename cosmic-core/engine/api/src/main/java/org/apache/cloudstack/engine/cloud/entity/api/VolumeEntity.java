package org.apache.cloudstack.engine.cloud.entity.api;

import org.apache.cloudstack.engine.datacenter.entity.api.StorageEntity;
import org.apache.cloudstack.engine.entity.api.CloudStackEntity;
import org.apache.cloudstack.engine.subsystem.api.storage.type.VolumeType;

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
