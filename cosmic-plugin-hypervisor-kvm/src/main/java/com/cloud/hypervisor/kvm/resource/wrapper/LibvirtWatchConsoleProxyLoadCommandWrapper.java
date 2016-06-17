//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.ResourceWrapper;
import com.cloud.resource.ServerResource;

@ResourceWrapper(handles = WatchConsoleProxyLoadCommand.class)
public class LibvirtWatchConsoleProxyLoadCommandWrapper
    extends LibvirtConsoleProxyLoadCommandWrapper<WatchConsoleProxyLoadCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final Command command, final ServerResource serverResource) {
    final WatchConsoleProxyLoadCommand cmd = (WatchConsoleProxyLoadCommand) command;

    final long proxyVmId = cmd.getProxyVmId();
    final String proxyVmName = cmd.getProxyVmName();
    final String proxyManagementIp = cmd.getProxyManagementIp();
    final int proxyCmdPort = cmd.getProxyCmdPort();

    return executeProxyLoadScan(cmd, proxyVmId, proxyVmName, proxyManagementIp, proxyCmdPort);
  }
}