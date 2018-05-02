package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.storage.KvmStoragePool;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.to.VolumeTO;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = DestroyCommand.class)
public final class LibvirtDestroyCommandWrapper
        extends CommandWrapper<DestroyCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtDestroyCommandWrapper.class);

    @Override
    public Answer execute(final DestroyCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final VolumeTO vol = command.getVolume();
        try {
            final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
            final KvmStoragePool pool = storagePoolMgr.getStoragePool(vol.getPoolType(), vol.getPoolUuid());
            pool.deletePhysicalDisk(vol.getPath(), null);
            return new Answer(command, true, "Success");
        } catch (final CloudRuntimeException e) {
            s_logger.debug("Failed to delete volume: " + e.toString());
            return new Answer(command, false, e.toString());
        }
    }
}
