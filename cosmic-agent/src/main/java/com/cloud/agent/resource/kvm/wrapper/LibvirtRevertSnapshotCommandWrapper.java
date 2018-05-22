package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.storage.KvmPhysicalDisk;
import com.cloud.agent.resource.kvm.storage.KvmStoragePool;
import com.cloud.agent.resource.kvm.storage.KvmStoragePoolManager;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.RevertSnapshotCommand;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.to.DataStoreTO;
import com.cloud.legacymodel.to.NfsTO;
import com.cloud.legacymodel.to.PrimaryDataStoreTO;
import com.cloud.legacymodel.to.SnapshotObjectTO;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.utils.script.Script;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = RevertSnapshotCommand.class)
public final class LibvirtRevertSnapshotCommandWrapper
        extends LibvirtCommandWrapper<RevertSnapshotCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtRevertSnapshotCommandWrapper.class);

    @Override
    public Answer execute(final RevertSnapshotCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final SnapshotObjectTO snapshot = command.getData();
        final VolumeObjectTO volume = snapshot.getVolume();
        final PrimaryDataStoreTO primaryStore = (PrimaryDataStoreTO) volume.getDataStore();
        final DataStoreTO snapshotImageStore = snapshot.getDataStore();
        if (!(snapshotImageStore instanceof NfsTO)) {
            return new Answer(command, false, "revert snapshot on object storage is not implemented yet");
        }
        final NfsTO nfsImageStore = (NfsTO) snapshotImageStore;

        final String secondaryStoragePoolUrl = nfsImageStore.getUrl();

        final String volumePath = volume.getPath();
        String snapshotPath = null;
        String snapshotRelPath = null;
        KvmStoragePool secondaryStoragePool = null;
        try {
            final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
            secondaryStoragePool = storagePoolMgr.getStoragePoolByUri(secondaryStoragePoolUrl);
            final String ssPmountPath = secondaryStoragePool.getLocalPath();
            snapshotRelPath = snapshot.getPath();
            snapshotPath = ssPmountPath + File.separator + snapshotRelPath;

            final KvmPhysicalDisk snapshotDisk = storagePoolMgr.getPhysicalDisk(primaryStore.getPoolType(),
                    primaryStore.getUuid(), volumePath);
            final KvmStoragePool primaryPool = snapshotDisk.getPool();

            if (primaryPool.getType() == StoragePoolType.RBD) {
                return new Answer(command, false, "revert snapshot to RBD is not implemented yet");
            } else {
                final Script cmd = new Script(libvirtComputingResource.manageSnapshotPath(),
                        libvirtComputingResource.getCmdsTimeout(), s_logger);
                cmd.add("-v", snapshotPath);
                cmd.add("-n", snapshotDisk.getName());
                cmd.add("-p", snapshotDisk.getPath());
                final String result = cmd.execute();
                if (result != null) {
                    s_logger.debug("Failed to revert snaptshot: " + result);
                    return new Answer(command, false, result);
                }
            }

            return new Answer(command, true, "RevertSnapshotCommand executes successfully");
        } catch (final CloudRuntimeException e) {
            return new Answer(command, false, e.toString());
        }
    }
}
