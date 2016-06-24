//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.StartAnswer;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.resource.virtualnetwork.VirtualRoutingResource;
import com.cloud.exception.InternalErrorException;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef;
import com.cloud.hypervisor.kvm.storage.KvmStoragePoolManager;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.vm.VirtualMachine;

import java.net.URISyntaxException;
import java.util.List;

import org.libvirt.Connect;
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
        vmSpec.setVncAddr(command.getHostIp());
        final String vmName = vmSpec.getName();
        LibvirtVmDef vm = null;

        DomainState state = DomainState.VIR_DOMAIN_SHUTOFF;
        final KvmStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
        Connect conn = null;
        try {
            final NicTO[] nics = vmSpec.getNics();

            vm = libvirtComputingResource.createVmFromSpec(vmSpec);
            conn = libvirtUtilitiesHelper.getConnectionByType(vm.getHvsType());
            libvirtComputingResource.createVbd(conn, vmSpec, vmName, vm);

            if (!storagePoolMgr.connectPhysicalDisksViaVmSpec(vmSpec)) {
                return new StartAnswer(command, "Failed to connect physical disks to host");
            }

            libvirtComputingResource.createVifs(vmSpec, vm);

            s_logger.debug("starting " + vmName + ": " + vm.toString());
            libvirtComputingResource.startVm(conn, vmName, vm.toString());

            for (final NicTO nic : nics) {
                if (nic.isSecurityGroupEnabled() || nic.getIsolationUri() != null
                        && nic.getIsolationUri().getScheme().equalsIgnoreCase(IsolationType.Ec2.toString())) {
                    if (vmSpec.getType() != VirtualMachine.Type.User) {
                        libvirtComputingResource.configureDefaultNetworkRulesForSystemVm(conn, vmName);
                        break;
                    } else {
                        final List<String> nicSecIps = nic.getNicSecIps();
                        final String secIpsStr;
                        final StringBuilder sb = new StringBuilder();
                        if (nicSecIps != null) {
                            for (final String ip : nicSecIps) {
                                sb.append(ip).append(":");
                            }
                            secIpsStr = sb.toString();
                        } else {
                            secIpsStr = "0:";
                        }
                        libvirtComputingResource.defaultNetworkRules(conn, vmName, nic, vmSpec.getId(), secIpsStr);
                    }
                }
            }

            // pass cmdline info to system vms
            if (vmSpec.getType() != VirtualMachine.Type.User) {
                // wait and try passCmdLine for 5 minutes at most for CLOUDSTACK-2823
                String controlIp = null;
                for (final NicTO nic : nics) {
                    if (nic.getType() == TrafficType.Control) {
                        controlIp = nic.getIp();
                        break;
                    }
                }
                for (int count = 0; count < 30; count++) {
                    libvirtComputingResource.passCmdLine(vmName, vmSpec.getBootArgs());
                    // check router is up?
                    final VirtualRoutingResource virtRouterResource = libvirtComputingResource.getVirtRouterResource();
                    final boolean result = virtRouterResource.connect(controlIp, 1, 5000);
                    if (result) {
                        break;
                    }
                }
            }

            state = DomainState.VIR_DOMAIN_RUNNING;
            return new StartAnswer(command);
        } catch (final LibvirtException e) {
            s_logger.warn("LibvirtException ", e);
            if (conn != null) {
                libvirtComputingResource.handleVmStartFailure(conn, vmName, vm);
            }
            return new StartAnswer(command, e.getMessage());
        } catch (final InternalErrorException e) {
            s_logger.warn("InternalErrorException ", e);
            if (conn != null) {
                libvirtComputingResource.handleVmStartFailure(conn, vmName, vm);
            }
            return new StartAnswer(command, e.getMessage());
        } catch (final URISyntaxException e) {
            s_logger.warn("URISyntaxException ", e);
            if (conn != null) {
                libvirtComputingResource.handleVmStartFailure(conn, vmName, vm);
            }
            return new StartAnswer(command, e.getMessage());
        } finally {
            if (state != DomainState.VIR_DOMAIN_RUNNING) {
                storagePoolMgr.disconnectPhysicalDisksViaVmSpec(vmSpec);
            }
        }
    }
}
