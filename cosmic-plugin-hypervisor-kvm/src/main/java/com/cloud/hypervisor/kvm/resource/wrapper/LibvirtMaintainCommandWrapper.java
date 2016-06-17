//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.MaintainAnswer;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = MaintainCommand.class)
public final class LibvirtMaintainCommandWrapper
    extends CommandWrapper<MaintainCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final MaintainCommand command, final LibvirtComputingResource libvirtComputingResource) {
    return new MaintainAnswer(command);
  }
}