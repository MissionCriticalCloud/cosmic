//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.RebootRouterCommand;
import com.cloud.agent.resource.virtualnetwork.VirtualRoutingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = RebootRouterCommand.class)
public final class LibvirtRebootRouterCommandWrapper
    extends CommandWrapper<RebootRouterCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final RebootRouterCommand command, final LibvirtComputingResource libvirtComputingResource) {
    final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();

    final RebootCommand rebootCommand = new RebootCommand(command.getVmName());
    final Answer answer = wrapper.execute(rebootCommand, libvirtComputingResource);

    final VirtualRoutingResource virtualRouterResource = libvirtComputingResource.getVirtRouterResource();
    if (virtualRouterResource.connect(command.getPrivateIpAddress())) {
      libvirtComputingResource.networkUsage(command.getPrivateIpAddress(), "create", null);

      return answer;
    } else {
      return new Answer(command, false, "Failed to connect to virtual router " + command.getVmName());
    }
  }
}