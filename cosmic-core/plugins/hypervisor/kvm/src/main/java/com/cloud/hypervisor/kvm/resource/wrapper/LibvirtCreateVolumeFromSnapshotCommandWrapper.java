package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateVolumeFromSnapshotAnswer;
import com.cloud.agent.api.CreateVolumeFromSnapshotCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.storage.KvmPhysicalDisk;
import com.cloud.hypervisor.kvm.storage.KvmStoragePool;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.UUID;

@ResourceWrapper(handles = CreateVolumeFromSnapshotCommand.class)
public final class LibvirtCreateVolumeFromSnapshotCommandWrapper
        extends CommandWrapper<CreateVolumeFromSnapshotCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final CreateVolumeFromSnapshotCommand command,
                          final LibvirtComputingResource libvirtComputingResource) {
        try {

            String snapshotPath = command.getSnapshotUuid();
            final int index = snapshotPath.lastIndexOf("/");
            snapshotPath = snapshotPath.substring(0, index);

            final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
            final KvmStoragePool secondaryPool = storagePoolMgr.getStoragePoolByUri(
                    command.getSecondaryStorageUrl() + snapshotPath);
            final KvmPhysicalDisk snapshot = secondaryPool.getPhysicalDisk(command.getSnapshotName());

            final String primaryUuid = command.getPrimaryStoragePoolNameLabel();

            final StorageFilerTO pool = command.getPool();
            final KvmStoragePool primaryPool = storagePoolMgr.getStoragePool(pool.getType(), primaryUuid);

            final String volUuid = UUID.randomUUID().toString();
            final KvmPhysicalDisk disk = storagePoolMgr.copyPhysicalDisk(snapshot, volUuid, primaryPool, 0);

            if (disk == null) {
                throw new NullPointerException("Disk was not successfully copied to the new storage.");
            }

            return new CreateVolumeFromSnapshotAnswer(command, true, "", disk.getName());
        } catch (final CloudRuntimeException e) {
            return new CreateVolumeFromSnapshotAnswer(command, false, e.toString(), null);
        } catch (final Exception e) {
            return new CreateVolumeFromSnapshotAnswer(command, false, e.toString(), null);
        }
    }
}
