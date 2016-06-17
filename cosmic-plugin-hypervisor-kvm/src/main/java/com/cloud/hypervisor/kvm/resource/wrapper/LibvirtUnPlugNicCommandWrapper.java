//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.List;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.UnPlugNicAnswer;
import com.cloud.agent.api.UnPlugNicCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.InterfaceDef;
import com.cloud.hypervisor.kvm.resource.VifDriver;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = UnPlugNicCommand.class)
public final class LibvirtUnPlugNicCommandWrapper
    extends CommandWrapper<UnPlugNicCommand, Answer, LibvirtComputingResource> {

  private static final Logger s_logger = LoggerFactory.getLogger(LibvirtUnPlugNicCommandWrapper.class);

  @Override
  public Answer execute(final UnPlugNicCommand command, final LibvirtComputingResource libvirtComputingResource) {
    final NicTO nic = command.getNic();
    final String vmName = command.getVmName();
    Domain vm = null;
    try {
      final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

      final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
      vm = libvirtComputingResource.getDomain(conn, vmName);
      final List<InterfaceDef> pluggedNics = libvirtComputingResource.getInterfaces(conn, vmName);

      for (final InterfaceDef pluggedNic : pluggedNics) {
        if (pluggedNic.getMacAddress().equalsIgnoreCase(nic.getMac())) {
          vm.detachDevice(pluggedNic.toString());
          // We don't know which "traffic type" is associated with
          // each interface at this point, so inform all vif drivers
          for (final VifDriver vifDriver : libvirtComputingResource.getAllVifDrivers()) {
            vifDriver.unplug(pluggedNic);
          }
          return new UnPlugNicAnswer(command, true, "success");
        }
      }
      return new UnPlugNicAnswer(command, true, "success");
    } catch (final LibvirtException e) {
      final String msg = " Unplug Nic failed due to " + e.toString();
      s_logger.warn(msg, e);
      return new UnPlugNicAnswer(command, false, msg);
    } finally {
      if (vm != null) {
        try {
          vm.free();
        } catch (final LibvirtException l) {
          s_logger.trace("Ignoring libvirt error.", l);
        }
      }
    }
  }
}