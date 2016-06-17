//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.resource.virtualnetwork.VirtualRoutingResource;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = NetworkElementCommand.class)
public final class LibvirtNetworkElementCommandWrapper
    extends CommandWrapper<NetworkElementCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final NetworkElementCommand command, final LibvirtComputingResource libvirtComputingResource) {
    final VirtualRoutingResource virtRouterResource = libvirtComputingResource.getVirtRouterResource();
    return virtRouterResource.executeRequest(command);
  }
}