package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.List;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckNetworkAnswer;
import com.cloud.agent.api.CheckNetworkCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.network.PhysicalNetworkSetupInfo;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = CheckNetworkCommand.class)
public final class LibvirtCheckNetworkCommandWrapper
    extends CommandWrapper<CheckNetworkCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final CheckNetworkCommand command, final LibvirtComputingResource libvirtComputingResource) {
    final List<PhysicalNetworkSetupInfo> phyNics = command.getPhysicalNetworkInfoList();
    String errMsg = null;

    for (final PhysicalNetworkSetupInfo nic : phyNics) {
      if (!libvirtComputingResource.checkNetwork(nic.getGuestNetworkName())) {
        errMsg = "Can not find network: " + nic.getGuestNetworkName();
        break;
      } else if (!libvirtComputingResource.checkNetwork(nic.getPrivateNetworkName())) {
        errMsg = "Can not find network: " + nic.getPrivateNetworkName();
        break;
      } else if (!libvirtComputingResource.checkNetwork(nic.getPublicNetworkName())) {
        errMsg = "Can not find network: " + nic.getPublicNetworkName();
        break;
      }
    }

    if (errMsg != null) {
      return new CheckNetworkAnswer(command, false, errMsg);
    } else {
      return new CheckNetworkAnswer(command, true, null);
    }
  }
}