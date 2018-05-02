package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.WatchConsoleProxyLoadCommand;
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
