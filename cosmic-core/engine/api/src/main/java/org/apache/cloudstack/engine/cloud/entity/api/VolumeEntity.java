package org.apache.cloudstack.engine.cloud.entity.api;

import org.apache.cloudstack.engine.datacenter.entity.api.StorageEntity;
import org.apache.cloudstack.engine.entity.api.CloudStackEntity;
import org.apache.cloudstack.engine.subsystem.api.storage.disktype.DiskFormat;
import org.apache.cloudstack.engine.subsystem.api.storage.type.VolumeType;

public interface VolumeEntity extends CloudStackEntity {

    /**
     * Take a snapshot of the volume
     */
    SnapshotEntity takeSnapshotOf(boolean full);

    /**
     * Make a reservation to do storage migration
     *
     * @param expirationTime time in seconds the reservation is cancelled
     * @return reservation token
     */
    String reserveForMigration(long expirationTime);

    /**
     * Migrate using a reservation.
     *
     * @param reservationToken reservation token
     */
    void migrate(String reservationToken);

    /**
     * Setup for a copy of this volume.
     *
     * @return destination to copy to
     */
    VolumeEntity setupForCopy();

    /**
     * Perform the copy
     *
     * @param dest copy to this volume
     */
    void copy(VolumeEntity dest);

    /**
     * Attach to the vm
     *
     * @param vm       vm to attach to
     * @param deviceId device id to use
     */
    void attachTo(String vm, long deviceId);

    /**
     * Detach from the vm
     */
    void detachFrom();

    /**
     * Destroy the volume
     */
    void destroy();

    long getSize();

    DiskFormat getDiskType();

    VolumeType getType();

    StorageEntity getDataStore();
}
