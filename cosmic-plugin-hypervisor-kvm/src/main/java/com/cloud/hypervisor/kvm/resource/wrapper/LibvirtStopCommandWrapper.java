//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.List;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.StopAnswer;
import com.cloud.agent.api.StopCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.DiskDef;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.InterfaceDef;
import com.cloud.hypervisor.kvm.resource.VifDriver;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

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

      final List<DiskDef> disks = libvirtComputingResource.getDisks(conn, vmName);
      final List<InterfaceDef> ifaces = libvirtComputingResource.getInterfaces(conn, vmName);

      libvirtComputingResource.destroyNetworkRulesForVm(conn, vmName);
      final String result = libvirtComputingResource.stopVm(conn, vmName);
      if (result == null) {
        for (final DiskDef disk : disks) {
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