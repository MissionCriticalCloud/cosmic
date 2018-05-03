package com.cloud.storage;

import com.cloud.api.command.user.volume.AttachVolumeCmd;
import com.cloud.api.command.user.volume.CreateVolumeCmd;
import com.cloud.api.command.user.volume.DetachVolumeCmd;
import com.cloud.api.command.user.volume.ExtractVolumeCmd;
import com.cloud.api.command.user.volume.GetUploadParamsForVolumeCmd;
import com.cloud.api.command.user.volume.MigrateVolumeCmd;
import com.cloud.api.command.user.volume.ResizeVolumeCmd;
import com.cloud.api.command.user.volume.UploadVolumeCmd;
import com.cloud.api.response.GetUploadParamsResponse;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.user.Account;
import com.cloud.model.enumeration.DiskControllerType;

import java.net.MalformedURLException;

public interface VolumeApiService {
    /**
     * Creates the database object for a volume based on the given criteria
     *
     * @param cmd the API command wrapping the criteria (account/domainId [admin only], zone, diskOffering, snapshot,
     *            name)
     * @return the volume object
     */
    Volume allocVolume(CreateVolumeCmd cmd) throws ResourceAllocationException;

    /**
     * Creates the volume based on the given criteria
     *
     * @param cmd the API command wrapping the criteria (account/domainId [admin only], zone, diskOffering, snapshot,
     *            name)
     * @return the volume object
     */
    Volume createVolume(CreateVolumeCmd cmd);

    /**
     * Resizes the volume based on the given criteria
     *
     * @param cmd the API command wrapping the criteria
     * @return the volume object
     * @throws ResourceAllocationException
     */
    Volume resizeVolume(ResizeVolumeCmd cmd) throws ResourceAllocationException;

    Volume migrateVolume(MigrateVolumeCmd cmd);

    /**
     * Uploads the volume to secondary storage
     *
     * @param UploadVolumeCmdByAdmin cmd
     * @return Volume object
     */
    Volume uploadVolume(UploadVolumeCmd cmd) throws ResourceAllocationException;

    GetUploadParamsResponse uploadVolume(GetUploadParamsForVolumeCmd cmd) throws ResourceAllocationException, MalformedURLException;

    boolean deleteVolume(long volumeId, Account caller) throws ConcurrentOperationException;

    Volume attachVolumeToVM(AttachVolumeCmd command);

    Volume detachVolumeFromVM(DetachVolumeCmd cmmd);

    Snapshot takeSnapshot(Long volumeId, Long policyId, Long snapshotId, Account account, boolean quiescevm) throws ResourceAllocationException;

    Snapshot allocSnapshot(Long volumeId, Long policyId, String snapshotName) throws ResourceAllocationException;

    Volume updateVolume(long volumeId, String path, String state, Long storageId, Boolean displayVolume, String customId, long owner, String chainInfo, DiskControllerType diskControllerType);

    /**
     * Extracts the volume to a particular location.
     *
     * @param cmd the command specifying url (where the volume needs to be extracted to), zoneId (zone where the volume
     *            exists),
     *            id (the id of the volume)
     */
    String extractVolume(ExtractVolumeCmd cmd);

    boolean isDisplayResourceEnabled(Long id);

    void updateDisplay(Volume volume, Boolean displayVolume);

    Snapshot allocSnapshotForVm(Long vmId, Long volumeId, String snapshotName) throws ResourceAllocationException;
}
