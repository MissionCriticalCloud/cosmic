package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.common.resource.ServerResource;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.WatchConsoleProxyLoadCommand;
import com.cloud.resource.ResourceWrapper;

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
