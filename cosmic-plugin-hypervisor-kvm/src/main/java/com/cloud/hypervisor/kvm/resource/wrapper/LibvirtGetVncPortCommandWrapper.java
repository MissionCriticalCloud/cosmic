//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetVncPortAnswer;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

@ResourceWrapper(handles = GetVncPortCommand.class)
public final class LibvirtGetVncPortCommandWrapper
    extends CommandWrapper<GetVncPortCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final GetVncPortCommand command, final LibvirtComputingResource libvirtComputingResource) {
    try {
      final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

      final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(command.getName());
      final Integer vncPort = libvirtComputingResource.getVncPort(conn, command.getName());
      return new GetVncPortAnswer(command, libvirtComputingResource.getPrivateIp(), 5900 + vncPort);
    } catch (final LibvirtException e) {
      return new GetVncPortAnswer(command, e.toString());
    }
  }
}