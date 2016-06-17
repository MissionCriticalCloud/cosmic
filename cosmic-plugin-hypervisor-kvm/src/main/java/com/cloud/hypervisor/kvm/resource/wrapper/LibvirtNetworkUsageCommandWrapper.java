//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.NetworkUsageAnswer;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = NetworkUsageCommand.class)
public final class LibvirtNetworkUsageCommandWrapper
    extends CommandWrapper<NetworkUsageCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final NetworkUsageCommand command, final LibvirtComputingResource libvirtComputingResource) {
    if (command.isForVpc()) {
      if (command.getOption() != null && command.getOption().equals("create")) {
        final String result = libvirtComputingResource.configureVpcNetworkUsage(command.getPrivateIP(),
            command.getGatewayIP(), "create", command.getVpcCIDR());
        final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, result, 0L, 0L);
        return answer;
      } else if (command.getOption() != null
          && (command.getOption().equals("get") || command.getOption().equals("vpn"))) {
        final long[] stats = libvirtComputingResource.getVpcNetworkStats(command.getPrivateIP(), command.getGatewayIP(),
            command.getOption());
        final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, "", stats[0], stats[1]);
        return answer;
      } else {
        final String result = libvirtComputingResource.configureVpcNetworkUsage(command.getPrivateIP(),
            command.getGatewayIP(), command.getOption(), command.getVpcCIDR());
        final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, result, 0L, 0L);
        return answer;
      }
    } else {
      if (command.getOption() != null && command.getOption().equals("create")) {
        final String result = libvirtComputingResource.networkUsage(command.getPrivateIP(), "create", null);
        final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, result, 0L, 0L);
        return answer;
      }
      final long[] stats = libvirtComputingResource.getNetworkStats(command.getPrivateIP());
      final NetworkUsageAnswer answer = new NetworkUsageAnswer(command, "", stats[0], stats[1]);
      return answer;
    }
  }
}