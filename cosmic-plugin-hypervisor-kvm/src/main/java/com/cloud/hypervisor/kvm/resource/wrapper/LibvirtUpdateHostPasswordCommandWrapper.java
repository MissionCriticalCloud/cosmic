//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.UpdateHostPasswordCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.utils.script.Script;

@ResourceWrapper(handles = UpdateHostPasswordCommand.class)
public final class LibvirtUpdateHostPasswordCommandWrapper
    extends CommandWrapper<UpdateHostPasswordCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final UpdateHostPasswordCommand command,
      final LibvirtComputingResource libvirtComputingResource) {
    final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

    final String username = command.getUsername();
    final String newPassword = command.getNewPassword();

    final Script script = libvirtUtilitiesHelper.buildScript(libvirtComputingResource.getUpdateHostPasswdPath());
    script.add(username, newPassword);
    final String result = script.execute();

    if (result != null) {
      return new Answer(command, false, result);
    }
    return new Answer(command);
  }
}