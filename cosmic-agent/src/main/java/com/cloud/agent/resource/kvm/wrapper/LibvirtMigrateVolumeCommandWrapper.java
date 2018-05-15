package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.xml.LibvirtDiskDef;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.MigrateVolumeAnswer;
import com.cloud.legacymodel.communication.command.MigrateVolumeCommand;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.event.BlockJobListener;
import org.libvirt.flags.DomainBlockJobAbortFlags;
import org.libvirt.parameters.DomainBlockCopyParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = MigrateVolumeCommand.class)
public final class LibvirtMigrateVolumeCommandWrapper extends CommandWrapper<MigrateVolumeCommand, Answer, LibvirtComputingResource> {

    private static final Logger logger = LoggerFactory.getLogger(LibvirtMigrateVolumeCommandWrapper.class);

    @Override
    public Answer execute(final MigrateVolumeCommand command, final LibvirtComputingResource libvirtComputingResource) {
        String result = null;

        final String vmName = command.getAttachedVmName();

        LibvirtDiskDef disk = null;
        final List<LibvirtDiskDef> disks;

        boolean isMigrationSuccessfull = false;

        Domain dm = null;
        Connect conn = null;

        String currentVolumePath = null;
        final String newVolumePath;

        final CountDownLatch completeSignal = new CountDownLatch(1);

        final BlockJobListener blockJobListener = new BlockJobListener() {
            @Override
            public void onBlockJobCompleted(final Domain domain, final String disk, final int type) throws LibvirtException {
                onBlockJobReady(domain, disk, type);
            }

            @Override
            public void onBlockJobFailed(final Domain domain, final String disk, final int type) throws LibvirtException {
                throw new LibvirtException("BlockJobFailed");
            }

            @Override
            public void onBlockJobCanceled(final Domain domain, final String disk, final int type) throws LibvirtException {
                throw new LibvirtException("BlockJobCanceled");
            }

            @Override
            public void onBlockJobReady(final Domain domain, final String disk, final int type) throws LibvirtException {
                domain.blockJobAbort(disk, DomainBlockJobAbortFlags.VIR_DOMAIN_BLOCK_JOB_ABORT_PIVOT);
                completeSignal.countDown();
            }
        };

        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

            conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
            disks = libvirtComputingResource.getDisks(conn, vmName);
            dm = conn.domainLookupByName(vmName);
            newVolumePath = "/mnt/" + command.getPool().getUuid() + "/" + command.getVolumePath();

            for (final LibvirtDiskDef diskDef : disks) {
                if (diskDef.getDiskPath().contains(command.getVolumePath())) {
                    disk = diskDef;
                    break;
                }
            }

            logger.debug("Registering block job listener with libvirt for domain " + dm.getName());
            dm.addBlockJobListener(blockJobListener);

            if (disk != null) {
                currentVolumePath = disk.getDiskPath();

                disk.setDiskPath(newVolumePath);

                logger.debug("Starting block copy for domain " + dm.getName() + " from " + currentVolumePath + " to " + newVolumePath);
                dm.blockCopy(currentVolumePath, disk.toString(), new DomainBlockCopyParameters(), 0);
            } else {
                throw new LibvirtException("Couldn't find disk: " + command.getVolumePath() + " on vm: " + dm.getName());
            }

            logger.debug("Waiting for block copy for domain " + dm.getName() + " from " + currentVolumePath + " to " + newVolumePath + " to finish");
            completeSignal.await();

            logger.debug("Refreshing storage pool " + command.getPool().getUuid());
            final StoragePool storagePool = conn.storagePoolLookupByUUIDString(command.getPool().getUuid());
            storagePool.refresh(0);

            isMigrationSuccessfull = true;
        } catch (final LibvirtException | InterruptedException e) {
            logger.debug("Can't migrate disk: " + e.getMessage());
            result = e.getMessage();
        } finally {
            try {
                if (dm != null) {
                    dm.free();
                }

                // Stop block job listener
                if (conn != null) {
                    conn.removeBlockJobListener(blockJobListener);
                }
            } catch (final LibvirtException e) {
                logger.debug("Ignoring libvirt error.", e);
            }
        }

        try {
            if (isMigrationSuccessfull && conn != null) {
                logger.debug("Cleaning up old disk " + currentVolumePath);
                final StorageVol storageVol = conn.storageVolLookupByPath(currentVolumePath);
                storageVol.delete(0);
            }
        } catch (final LibvirtException e) {
            logger.error("Cleaning up old disk " + currentVolumePath + " failed!", e);
        }

        return new MigrateVolumeAnswer(command, result == null, result, command.getVolumePath());
    }
}
