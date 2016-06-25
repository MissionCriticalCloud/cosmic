package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ManageSnapshotAnswer;
import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.storage.KvmPhysicalDisk;
import com.cloud.hypervisor.kvm.storage.KvmStoragePool;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.script.Script;

import java.io.File;
import java.text.MessageFormat;

import com.ceph.rados.IoCTX;
import com.ceph.rados.Rados;
import com.ceph.rbd.Rbd;
import com.ceph.rbd.RbdImage;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.DomainSnapshot;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = ManageSnapshotCommand.class)
public final class LibvirtManageSnapshotCommandWrapper
        extends CommandWrapper<ManageSnapshotCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtManageSnapshotCommandWrapper.class);

    @Override
    public Answer execute(final ManageSnapshotCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final String snapshotName = command.getSnapshotName();
        final String snapshotPath = command.getSnapshotPath();
        final String vmName = command.getVmName();
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
            final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
            DomainState state = null;
            Domain vm = null;
            if (vmName != null) {
                try {
                    vm = libvirtComputingResource.getDomain(conn, command.getVmName());
                    state = vm.getInfo().state;
                } catch (final LibvirtException e) {
                    s_logger.trace("Ignoring libvirt error.", e);
                }
            }

            final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
            final StorageFilerTO pool = command.getPool();
            final KvmStoragePool primaryPool = storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid());

            final KvmPhysicalDisk disk = primaryPool.getPhysicalDisk(command.getVolumePath());
            if (state == DomainState.VIR_DOMAIN_RUNNING && !primaryPool.isExternalSnapshot()) {

                final MessageFormat snapshotXml = new MessageFormat(
                        "   <domainsnapshot>" + "       <name>{0}</name>" + "          <domain>"
                                + "            <uuid>{1}</uuid>" + "        </domain>" + "    </domainsnapshot>");

                final String vmUuid = vm.getUUIDString();
                final Object[] args = new Object[]{snapshotName, vmUuid};
                final String snapshot = snapshotXml.format(args);
                s_logger.debug(snapshot);
                if (command.getCommandSwitch().equalsIgnoreCase(ManageSnapshotCommand.CREATE_SNAPSHOT)) {
                    vm.snapshotCreateXML(snapshot);
                } else {
                    final DomainSnapshot snap = vm.snapshotLookupByName(snapshotName);
                    snap.delete(0);
                }

        /*
         * libvirt on RHEL6 doesn't handle resume event emitted from qemu
         */
                vm = libvirtComputingResource.getDomain(conn, command.getVmName());
                state = vm.getInfo().state;
                if (state == DomainState.VIR_DOMAIN_PAUSED) {
                    vm.resume();
                }
            } else {
                if (primaryPool.getType() == StoragePoolType.RBD) {
                    try {
                        final Rados r = new Rados(primaryPool.getAuthUserName());
                        r.confSet("mon_host", primaryPool.getSourceHost() + ":" + primaryPool.getSourcePort());
                        r.confSet("key", primaryPool.getAuthSecret());
                        r.confSet("client_mount_timeout", "30");
                        r.connect();
                        s_logger.debug("Succesfully connected to Ceph cluster at " + r.confGet("mon_host"));

                        final IoCTX io = r.ioCtxCreate(primaryPool.getSourceDir());
                        final Rbd rbd = new Rbd(io);
                        final RbdImage image = rbd.open(disk.getName());

                        if (command.getCommandSwitch().equalsIgnoreCase(ManageSnapshotCommand.CREATE_SNAPSHOT)) {
                            s_logger.debug("Attempting to create RBD snapshot " + disk.getName() + "@" + snapshotName);
                            image.snapCreate(snapshotName);
                        } else {
                            s_logger.debug("Attempting to remove RBD snapshot " + disk.getName() + "@" + snapshotName);
                            image.snapRemove(snapshotName);
                        }

                        rbd.close(image);
                        r.ioCtxDestroy(io);
                    } catch (final Exception e) {
                        s_logger.error(
                                "A RBD snapshot operation on " + disk.getName() + " failed. The error was: " + e.getMessage());
                    }
                } else {
          /* VM is not running, create a snapshot by ourself */
                    final int cmdsTimeout = libvirtComputingResource.getCmdsTimeout();
                    final String manageSnapshotPath = libvirtComputingResource.manageSnapshotPath();

                    final Script scriptCommand = new Script(manageSnapshotPath, cmdsTimeout, s_logger);
                    if (command.getCommandSwitch().equalsIgnoreCase(ManageSnapshotCommand.CREATE_SNAPSHOT)) {
                        scriptCommand.add("-c", disk.getPath());
                    } else {
                        scriptCommand.add("-d", snapshotPath);
                    }

                    scriptCommand.add("-n", snapshotName);
                    final String result = scriptCommand.execute();
                    if (result != null) {
                        s_logger.debug("Failed to manage snapshot: " + result);
                        return new ManageSnapshotAnswer(command, false, "Failed to manage snapshot: " + result);
                    }
                }
            }
            return new ManageSnapshotAnswer(command, command.getSnapshotId(), disk.getPath() + File.separator + snapshotName,
                    true, null);
        } catch (final LibvirtException e) {
            s_logger.debug("Failed to manage snapshot: " + e.toString());
            return new ManageSnapshotAnswer(command, false, "Failed to manage snapshot: " + e.toString());
        }
    }
}
