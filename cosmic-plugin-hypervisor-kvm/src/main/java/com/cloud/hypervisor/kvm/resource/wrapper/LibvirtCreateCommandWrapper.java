package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VolumeTO;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.storage.KvmPhysicalDisk;
import com.cloud.hypervisor.kvm.storage.KvmStoragePool;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.DiskProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CreateCommand.class)
public final class LibvirtCreateCommandWrapper extends CommandWrapper<CreateCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtCreateCommandWrapper.class);

    @Override
    public Answer execute(final CreateCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final StorageFilerTO pool = command.getPool();
        final DiskProfile dskch = command.getDiskCharacteristics();
        KvmPhysicalDisk baseVol = null;
        KvmStoragePool primaryPool = null;
        KvmPhysicalDisk vol = null;
        final long disksize;
        try {
            final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
            primaryPool = storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid());
            disksize = dskch.getSize();

            if (command.getTemplateUrl() != null) {
                if (primaryPool.getType() == StoragePoolType.CLVM) {
                    vol = libvirtComputingResource.templateToPrimaryDownload(command.getTemplateUrl(), primaryPool,
                            dskch.getPath());
                } else {
                    baseVol = primaryPool.getPhysicalDisk(command.getTemplateUrl());
                    vol = storagePoolMgr.createDiskFromTemplate(baseVol, dskch.getPath(), dskch.getProvisioningType(),
                            primaryPool, 0);
                }
                if (vol == null) {
                    return new Answer(command, false, " Can't create storage volume on storage pool");
                }
            } else {
                vol = primaryPool.createPhysicalDisk(dskch.getPath(), dskch.getProvisioningType(), dskch.getSize());
                if (vol == null) {
                    return new Answer(command, false, " Can't create Physical Disk");
                }
            }
            final VolumeTO volume = new VolumeTO(command.getVolumeId(), dskch.getType(), pool.getType(), pool.getUuid(),
                    pool.getPath(), vol.getName(), vol.getName(), disksize,
                    null);

            volume.setBytesReadRate(dskch.getBytesReadRate());
            volume.setBytesWriteRate(dskch.getBytesWriteRate());
            volume.setIopsReadRate(dskch.getIopsReadRate());
            volume.setIopsWriteRate(dskch.getIopsWriteRate());
            volume.setCacheMode(dskch.getCacheMode());
            return new CreateAnswer(command, volume);
        } catch (final CloudRuntimeException e) {
            s_logger.debug("Failed to create volume: " + e.toString());
            return new CreateAnswer(command, e);
        }
    }
}
