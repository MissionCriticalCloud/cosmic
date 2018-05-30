package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.storage.KvmPhysicalDisk;
import com.cloud.agent.resource.kvm.storage.KvmStoragePool;
import com.cloud.agent.resource.kvm.storage.KvmStoragePoolManager;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CreateAnswer;
import com.cloud.legacymodel.communication.command.CreateCommand;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.storage.DiskProfile;
import com.cloud.legacymodel.to.StorageFilerTO;
import com.cloud.legacymodel.to.VolumeTO;
import com.cloud.model.enumeration.StoragePoolType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CreateCommand.class)
public final class LibvirtCreateCommandWrapper extends LibvirtCommandWrapper<CreateCommand, Answer, LibvirtComputingResource> {

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
                    vol = libvirtComputingResource.templateToPrimaryDownload(command.getTemplateUrl(), primaryPool, dskch.getPath());
                } else if (primaryPool.getType() == StoragePoolType.LVM) {
                    vol = libvirtComputingResource.templateToPrimaryDownload(command.getTemplateUrl(), primaryPool, dskch.getPath());
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
