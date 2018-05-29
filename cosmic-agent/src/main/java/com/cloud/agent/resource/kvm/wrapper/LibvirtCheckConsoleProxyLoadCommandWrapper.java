package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.AgentResource;
import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.CheckConsoleProxyLoadCommand;
import com.cloud.legacymodel.communication.command.Command;

@ResourceWrapper(handles = CheckConsoleProxyLoadCommand.class)
public class LibvirtCheckConsoleProxyLoadCommandWrapper extends LibvirtConsoleProxyLoadCommandWrapper<CheckConsoleProxyLoadCommand, Answer, LibvirtComputingResource> {

    @Override
    public Answer execute(final Command command, final AgentResource agentResource) {
        final CheckConsoleProxyLoadCommand cmd = (CheckConsoleProxyLoadCommand) command;

        final long proxyVmId = cmd.getProxyVmId();
        final String proxyVmName = cmd.getProxyVmName();
        final String proxyManagementIp = cmd.getProxyManagementIp();
        final int proxyCmdPort = cmd.getProxyCmdPort();

        return executeProxyLoadScan(cmd, proxyVmId, proxyVmName, proxyManagementIp, proxyCmdPort);
    }
}
