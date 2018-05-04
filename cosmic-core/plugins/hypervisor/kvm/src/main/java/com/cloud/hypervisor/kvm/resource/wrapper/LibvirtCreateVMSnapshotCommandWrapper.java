package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CreateVMSnapshotAnswer;
import com.cloud.legacymodel.communication.command.CreateVMSnapshotCommand;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CreateVMSnapshotCommand.class)
public final class LibvirtCreateVMSnapshotCommandWrapper extends CommandWrapper<CreateVMSnapshotCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtCreateVMSnapshotCommandWrapper.class);

    @Override
    public Answer execute(final CreateVMSnapshotCommand cmd, final LibvirtComputingResource libvirtComputingResource) {
        final String vmName = cmd.getVmName();
        final String vmSnapshotName = cmd.getTarget().getSnapshotName();

        Domain dm = null;
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
            final Connect conn = libvirtUtilitiesHelper.getConnection();
            dm = libvirtComputingResource.getDomain(conn, vmName);

            if (dm == null) {
                return new CreateVMSnapshotAnswer(cmd, false, "Create VM Snapshot Failed due to can not find vm: " + vmName);
            }

            final DomainState domainState = dm.getInfo().state;
            if (domainState != DomainState.VIR_DOMAIN_RUNNING) {
                return new CreateVMSnapshotAnswer(cmd, false, "Create VM Snapshot Failed due to  vm is not running: " + vmName + " with domainState = " + domainState);
            }

            final String vmSnapshotXML = "<domainsnapshot><name>" + vmSnapshotName + "</name><memory snapshot='internal' /></domainsnapshot>";

            dm.snapshotCreateXML(vmSnapshotXML);

            return new CreateVMSnapshotAnswer(cmd, cmd.getTarget(), cmd.getVolumeTOs());
        } catch (final LibvirtException e) {
            final String msg = " Create VM snapshot failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new CreateVMSnapshotAnswer(cmd, false, msg);
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
