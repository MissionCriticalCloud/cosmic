package com.cloud.agent.resource.kvm.wrapper;

import static org.libvirt.flags.DomainSnapshotRevertFlags.*;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.RevertToVMSnapshotAnswer;
import com.cloud.legacymodel.communication.command.RevertToVMSnapshotCommand;
import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.legacymodel.vm.VirtualMachine.PowerState;

import java.util.List;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainSnapshot;
import org.libvirt.LibvirtException;
import org.libvirt.flags.DomainSnapshotListFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = RevertToVMSnapshotCommand.class)
public final class LibvirtRevertToVMSnapshotCommandWrapper extends LibvirtCommandWrapper<RevertToVMSnapshotCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtRestoreVMSnapshotCommandWrapper.class);

    @Override
    public Answer execute(final RevertToVMSnapshotCommand cmd, final LibvirtComputingResource libvirtComputingResource) {
        final String vmName = cmd.getVmName();
        final List<VolumeObjectTO> listVolumeTo = cmd.getVolumeTOs();
        final VMSnapshot.Type vmSnapshotType = cmd.getTarget().getType();
        final Boolean snapshotMemory = vmSnapshotType == VMSnapshot.Type.DiskAndMemory;
        final PowerState vmState;

        Domain dm = null;
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
            final Connect conn = libvirtUtilitiesHelper.getConnection();
            dm = libvirtComputingResource.getDomain(conn, vmName);

            if (dm == null) {
                return new RevertToVMSnapshotAnswer(cmd, false, "Revert to VM Snapshot Failed due to can not find vm: " + vmName);
            }

            final DomainSnapshot snapshot = dm.snapshotLookupByName(cmd.getTarget().getSnapshotName());
            if (snapshot == null) {
                return new RevertToVMSnapshotAnswer(cmd, false, "Cannot find vmSnapshot with name: " + cmd.getTarget().getSnapshotName());
            }

            dm.revertToSnapshot(snapshot, VIR_DOMAIN_SNAPSHOT_REVERT_FORCE | VIR_DOMAIN_SNAPSHOT_REVERT_RUNNING);
            snapshot.free();

            if (!snapshotMemory) {
                dm.destroy();
                if (dm.isPersistent() == 1) {
                    dm.undefine();
                }
                vmState = PowerState.PowerOff;
            } else {
                vmState = PowerState.PowerOn;
            }

            return new RevertToVMSnapshotAnswer(cmd, listVolumeTo, vmState);
        } catch (final LibvirtException e) {
            final String msg = " Revert to VM snapshot failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new RevertToVMSnapshotAnswer(cmd, false, msg);
        } finally {
            if (dm != null) {
                try {
                    dm.free();
                } catch (final LibvirtException l) {
                    s_logger.trace("Ignoring libvirt error.", l);
                }
                ;
            }
        }
    }
}
