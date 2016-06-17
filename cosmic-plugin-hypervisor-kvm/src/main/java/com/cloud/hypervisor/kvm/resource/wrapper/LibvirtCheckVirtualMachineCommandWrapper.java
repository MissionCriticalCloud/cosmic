package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckVirtualMachineAnswer;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.vm.VirtualMachine.PowerState;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

@ResourceWrapper(handles = CheckVirtualMachineCommand.class)
public final class LibvirtCheckVirtualMachineCommandWrapper
    extends CommandWrapper<CheckVirtualMachineCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final CheckVirtualMachineCommand command,
      final LibvirtComputingResource libvirtComputingResource) {
    try {
      final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

      final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName());
      final PowerState state = libvirtComputingResource.getVmState(conn, command.getVmName());
      Integer vncPort = null;
      if (state == PowerState.PowerOn) {
        vncPort = libvirtComputingResource.getVncPort(conn, command.getVmName());
      }

      return new CheckVirtualMachineAnswer(command, state, vncPort);
    } catch (final LibvirtException e) {
      return new CheckVirtualMachineAnswer(command, e.getMessage());
    }
  }
}