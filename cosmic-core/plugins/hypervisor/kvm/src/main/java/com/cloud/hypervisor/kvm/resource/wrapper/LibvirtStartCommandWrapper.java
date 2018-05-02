package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.resource.virtualnetwork.VirtualRoutingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.vm.VirtualMachine;

import java.net.URISyntaxException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = StartCommand.class)
public final class LibvirtStartCommandWrapper extends CommandWrapper<StartCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtStartCommandWrapper.class);

    @Override
    public Answer execute(final StartCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final VirtualMachineTO vmSpec = command.getVirtualMachine();
        final String vmName = vmSpec.getName();
        LibvirtVmDef vm = null;

        DomainState state = DomainState.VIR_DOMAIN_SHUTOFF;
        final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
        Connect conn = null;
        try {
            vm = libvirtComputingResource.createVmFromSpec(vmSpec);
            conn = libvirtUtilitiesHelper.getConnectionByType(vm.getHvsType());

            final Long remainingMem = getFreeMemory(conn, libvirtComputingResource);
            if (remainingMem == null) {
                return new StartAnswer(command, "Failed to get free memory");
            } else if (remainingMem < vmSpec.getMinRam()) {
                return new StartAnswer(command, "Not enough memory on the host, remaining: " + remainingMem + ", asking: " + vmSpec.getMinRam());
            }

            final NicTO[] nics = vmSpec.getNics();

            libvirtComputingResource.createVbd(conn, vmSpec, vmName, vm);

            if (!storagePoolMgr.connectPhysicalDisksViaVmSpec(vmSpec)) {
                return new StartAnswer(command, "Failed to connect physical disks to host");
            }

            libvirtComputingResource.createVifs(vmSpec, vm);

            s_logger.debug("starting " + vmName + ": " + vm.toString());
            libvirtComputingResource.startVm(conn, vmName, vm.toString());

            // system vms
            if (vmSpec.getType() != VirtualMachine.Type.User) {

                // pass cmdline with config for the systemvm to configure itself
                if (libvirtComputingResource.passCmdLine(vmName, vmSpec.getBootArgs())) {
                    s_logger.debug("Passing cmdline succeeded");
                } else {
                    final String errorMessage = "Passing cmdline failed, aborting.";
                    s_logger.debug(errorMessage);
                    return new StartAnswer(command, errorMessage);
                }

                String controlIp = null;
                for (final NicTO nic : nics) {
                    if (nic.getType() == TrafficType.Control) {
                        controlIp = nic.getIp();
                        break;
                    }
                }

                // connect to the router by using its linklocal address (that should now be configured)
                s_logger.debug("Starting ssh attempts to " + controlIp);
                final VirtualRoutingResource virtRouterResource = libvirtComputingResource.getVirtRouterResource();

                if (!virtRouterResource.connect(controlIp, 30, 5000)) {
                    final String errorMessage = "Unable to login to router via linklocal address " + controlIp +
                            " after 30 tries, aborting.";
                    s_logger.debug(errorMessage);
                    return new StartAnswer(command, errorMessage);
                }
                s_logger.debug("Successfully completed ssh attempts to " + controlIp);
            }

            state = DomainState.VIR_DOMAIN_RUNNING;
            return new StartAnswer(command);
        } catch (final LibvirtException | InternalErrorException | URISyntaxException e) {
            s_logger.warn("Exception while starting VM: " + ExceptionUtils.getRootCauseMessage(e));
            if (conn != null) {
                libvirtComputingResource.handleVmStartFailure(vm);
            }
            return new StartAnswer(command, e.getMessage());
        } finally {
            if (state != DomainState.VIR_DOMAIN_RUNNING) {
                storagePoolMgr.disconnectPhysicalDisksViaVmSpec(vmSpec);
            }
        }
    }

    private Long getFreeMemory(final Connect conn, final LibvirtComputingResource libvirtComputingResource) {
        try {
            long allocatedMem = 0;
            final int[] ids = conn.listDomains();
            for (final int id : ids) {
                final Domain dm = conn.domainLookupByID(id);
                allocatedMem += dm.getMaxMemory() * 1024L;
                s_logger.debug("vm: " + dm.getName() + " mem: " + dm.getMaxMemory() * 1024L);
            }
            final Long remainingMem = libvirtComputingResource.getTotalMemory() - allocatedMem;
            s_logger.debug("remaining mem" + remainingMem);
            return remainingMem;
        } catch (final Exception e) {
            s_logger.debug("failed to get free memory", e);
            return null;
        }
    }
}
