//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.ResourceWrapper;
import com.cloud.resource.ServerResource;

@ResourceWrapper(handles = WatchConsoleProxyLoadCommand.class)
public final class CitrixWatchConsoleProxyLoadCommandWrapper extends CitrixConsoleProxyLoadCommandWrapper<WatchConsoleProxyLoadCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final Command command, final ServerResource serverResource) {
        final WatchConsoleProxyLoadCommand cmd = (WatchConsoleProxyLoadCommand) command;

        final long proxyVmId = cmd.getProxyVmId();
        final String proxyVmName = cmd.getProxyVmName();
        final String proxyManagementIp = cmd.getProxyManagementIp();
        final int cmdPort = cmd.getProxyCmdPort();

        return executeProxyLoadScan(command, proxyVmId, proxyVmName, proxyManagementIp, cmdPort);
    }
}
