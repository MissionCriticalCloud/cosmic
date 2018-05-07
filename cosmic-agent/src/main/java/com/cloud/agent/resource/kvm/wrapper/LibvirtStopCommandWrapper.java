package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.LibvirtVmDef.InterfaceDef;
import com.cloud.agent.resource.kvm.VifDriver;
import com.cloud.agent.resource.kvm.xml.LibvirtDiskDef;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.StopAnswer;
import com.cloud.legacymodel.communication.command.StopCommand;

import java.util.List;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = StopCommand.class)
public final class LibvirtStopCommandWrapper extends CommandWrapper<StopCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtStopCommandWrapper.class);

    @Override
    public Answer execute(final StopCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final String vmName = command.getVmName();

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

        if (command.checkBeforeCleanup()) {
            try {
                final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
                final Domain vm = conn.domainLookupByName(command.getVmName());
                if (vm != null && vm.getInfo().state == DomainState.VIR_DOMAIN_RUNNING) {
                    return new StopAnswer(command, "vm is still running on host", false);
                }
            } catch (final Exception e) {
                s_logger.debug("Failed to get vm status in case of checkboforecleanup is true", e);
            }
        }

        try {
            final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);

            final List<LibvirtDiskDef> disks = libvirtComputingResource.getDisks(conn, vmName);
            final List<InterfaceDef> ifaces = libvirtComputingResource.getInterfaces(conn, vmName);
            final String result = libvirtComputingResource.stopVm(conn, vmName, command.isForceStop());
            if (result == null) {
                for (final LibvirtDiskDef disk : disks) {
                    libvirtComputingResource.cleanupDisk(disk);
                }
                for (final InterfaceDef iface : ifaces) {
                    // We don't know which "traffic type" is associated with
                    // each interface at this point, so inform all vif drivers
                    for (final VifDriver vifDriver : libvirtComputingResource.getAllVifDrivers()) {
                        vifDriver.unplug(iface);
                    }
                }
            }

            return new StopAnswer(command, result, true);
        } catch (final LibvirtException e) {
            return new StopAnswer(command, e.getMessage(), false);
        }
    }
}
