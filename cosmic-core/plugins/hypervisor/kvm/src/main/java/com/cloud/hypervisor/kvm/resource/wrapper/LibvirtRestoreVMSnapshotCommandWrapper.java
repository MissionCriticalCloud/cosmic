package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.RestoreVMSnapshotAnswer;
import com.cloud.agent.api.RestoreVMSnapshotCommand;
import com.cloud.agent.api.VMSnapshotTO;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.legacymodel.vm.VirtualMachine.PowerState;

import java.util.List;
import java.util.Map;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = RestoreVMSnapshotCommand.class)
public final class LibvirtRestoreVMSnapshotCommandWrapper extends CommandWrapper<RestoreVMSnapshotCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtRestoreVMSnapshotCommandWrapper.class);

    @Override
    public Answer execute(final RestoreVMSnapshotCommand cmd, final LibvirtComputingResource libvirtComputingResource) {
        final String vmName = cmd.getVmName();
        final List<VolumeObjectTO> listVolumeTo = cmd.getVolumeTOs();
        final PowerState vmState = PowerState.PowerOn;

        Domain dm = null;
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
            final Connect conn = libvirtUtilitiesHelper.getConnection();
            dm = libvirtComputingResource.getDomain(conn, vmName);

            if (dm == null) {
                return new RestoreVMSnapshotAnswer(cmd, false, "Restore VM Snapshot Failed due to can not find vm: " + vmName);
            }
            final String xmlDesc = dm.getXMLDesc(0);

            final List<VMSnapshotTO> snapshots = cmd.getSnapshots();
            final Map<Long, VMSnapshotTO> snapshotAndParents = cmd.getSnapshotAndParents();
            for (final VMSnapshotTO snapshot : snapshots) {
                final VMSnapshotTO parent = snapshotAndParents.get(snapshot.getId());
                final String vmSnapshotXML = libvirtUtilitiesHelper.generateVMSnapshotXML(snapshot, parent, xmlDesc);
                s_logger.debug("Restoring vm snapshot " + snapshot.getSnapshotName() + " on " + vmName + " with XML:\n " + vmSnapshotXML);
                try {
                    int flags = 1; // VIR_DOMAIN_SNAPSHOT_CREATE_REDEFINE = 1
                    if (snapshot.getCurrent()) {
                        flags += 2; // VIR_DOMAIN_SNAPSHOT_CREATE_CURRENT = 2
                    }
                    dm.snapshotCreateXML(vmSnapshotXML, flags);
                } catch (final LibvirtException e) {
                    s_logger.debug("Failed to restore vm snapshot " + snapshot.getSnapshotName() + " on " + vmName);
                    return new RestoreVMSnapshotAnswer(cmd, false, e.toString());
                }
            }

            return new RestoreVMSnapshotAnswer(cmd, listVolumeTo, vmState);
        } catch (final LibvirtException e) {
            final String msg = " Restore snapshot failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new RestoreVMSnapshotAnswer(cmd, false, msg);
        } finally {
            if (dm != null) {
                try {
                    dm.free();
                } catch (final LibvirtException l) {
                    s_logger.trace("Ignoring libvirt error.", l);
                }
            }
        }
    }
}
