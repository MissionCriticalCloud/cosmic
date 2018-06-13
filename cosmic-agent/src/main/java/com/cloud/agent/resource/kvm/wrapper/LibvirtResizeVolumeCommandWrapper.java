package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.storage.KvmPhysicalDisk;
import com.cloud.agent.resource.kvm.storage.KvmStoragePool;
import com.cloud.agent.resource.kvm.storage.KvmStoragePoolManager;
import com.cloud.agent.resource.kvm.storage.utils.LVM;
import com.cloud.agent.resource.kvm.storage.utils.LVMException;
import com.cloud.agent.resource.kvm.storage.utils.QemuImg;
import com.cloud.agent.resource.kvm.storage.utils.QemuImgException;
import com.cloud.agent.resource.kvm.storage.utils.QemuImgFile;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.ResizeVolumeAnswer;
import com.cloud.legacymodel.communication.command.ResizeVolumeCommand;
import com.cloud.legacymodel.to.StorageFilerTO;
import com.cloud.model.enumeration.PhysicalDiskFormat;
import com.cloud.model.enumeration.StoragePoolType;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.flags.DomainBlockResizeFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = ResizeVolumeCommand.class)
public final class LibvirtResizeVolumeCommandWrapper extends LibvirtCommandWrapper<ResizeVolumeCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtResizeVolumeCommandWrapper.class);

    @Override
    public Answer execute(final ResizeVolumeCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final String volid = command.getPath();
        final long newSize = command.getNewSize();
        final long currentSize = command.getCurrentSize();
        final String vmInstanceName = command.getInstanceName();
        final boolean shrinkOk = command.getShrinkOk();
        final StorageFilerTO spool = command.getPool();

        if (currentSize == newSize) {
            s_logger.info("No need to resize volume: current size " + currentSize + " is same as new size " + newSize);
            return new ResizeVolumeAnswer(command, true, "success", currentSize);
        }

        final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
        final KvmStoragePool pool = storagePoolMgr.getStoragePool(spool.getType(), spool.getUuid());

        final KvmPhysicalDisk vol = pool.getPhysicalDisk(volid);
        final String path = vol.getPath();

        s_logger.debug("Resizing volume: " + path + "," + currentSize + "," + newSize + "," + vol.getFormat() + "," + vmInstanceName + "," + shrinkOk);
        if (pool.getType() == StoragePoolType.RBD) {
            s_logger.debug("Volume " + path + " is on a RBD storage pool. No need to query for additional information.");
        } else if (pool.getType() == StoragePoolType.LVM || pool.getType() == StoragePoolType.CLVM) {
            s_logger.debug("Volume " + path + " can be resized by libvirt. Asking libvirt to resize the volume.");

            final LVM lvm = new LVM(command.getWait());
            try {
                // 1. Resize the logical volume
                lvm.resize(newSize, vol.getPath());

                // 2. If the volume is attached to a virtualmachine, notify libvirt domain of the size change
                libvirtBlockResize(libvirtComputingResource, newSize, vmInstanceName, vol);
            } catch (final LVMException e) {
                // First step went wrong, nothing to clean up. Just return that it didn't work out.
                return new ResizeVolumeAnswer(command, false, e.toString());
            } catch (final LibvirtException e) {
                // Second step went wrong, we should resize the volume back to how it was!
                try {
                    lvm.resize(currentSize, vol.getPath());
                } catch (final LVMException e1) {
                    s_logger.error("Unable to reverse lv resize: " + e1);
                }
                return new ResizeVolumeAnswer(command, false, e.toString());
            }
        } else if (pool.getType() == StoragePoolType.NetworkFilesystem) {
            if (vol.getFormat() == PhysicalDiskFormat.QCOW2 && shrinkOk) {
                return new ResizeVolumeAnswer(command, false, "Unable to shrink volumes of type " + PhysicalDiskFormat.QCOW2);
            }

            try {
                if (vmInstanceName == null) {
                    final QemuImg qemuImg = new QemuImg(command.getWait());
                    qemuImg.resize(new QemuImgFile(vol.getPath(), vol.getFormat()), newSize);
                } else {
                    libvirtBlockResize(libvirtComputingResource, newSize, vmInstanceName, vol);
                }
            } catch (final LibvirtException | QemuImgException e) {
                return new ResizeVolumeAnswer(command, false, e.toString());
            }
        }

        /* fetch new size as seen from libvirt, don't want to assume anything */
        pool.refresh();
        final long finalSize = pool.getPhysicalDisk(volid).getVirtualSize();
        s_logger.debug("after resize, size reports as " + finalSize + ", requested " + newSize);
        return new ResizeVolumeAnswer(command, true, "success", finalSize);
    }

    private void libvirtBlockResize(final LibvirtComputingResource libvirtComputingResource, final long newSize, final String vmInstanceName, final KvmPhysicalDisk vol) throws LibvirtException {
        if (vmInstanceName != null) {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

            final Connect connection = libvirtUtilitiesHelper.getConnection();
            final Domain domain = connection.domainLookupByName(vmInstanceName);

            domain.blockResize(vol.getPath(), newSize, DomainBlockResizeFlags.BYTES);
        }
    }
}
