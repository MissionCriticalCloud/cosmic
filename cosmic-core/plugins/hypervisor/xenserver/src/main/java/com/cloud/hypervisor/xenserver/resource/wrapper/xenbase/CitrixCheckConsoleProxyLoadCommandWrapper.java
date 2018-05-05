package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.common.resource.ServerResource;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.CheckConsoleProxyLoadCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = CheckConsoleProxyLoadCommand.class)
public final class CitrixCheckConsoleProxyLoadCommandWrapper extends CitrixConsoleProxyLoadCommandWrapper<CheckConsoleProxyLoadCommand, Answer, CitrixResourceBase> {

    @Override
    public Answer execute(final Command command, final ServerResource serverResource) {
        final CheckConsoleProxyLoadCommand cmd = (CheckConsoleProxyLoadCommand) command;

        final long proxyVmId = cmd.getProxyVmId();
        final String proxyVmName = cmd.getProxyVmName();
        final String proxyManagementIp = cmd.getProxyManagementIp();
        final int cmdPort = cmd.getProxyCmdPort();

        return executeProxyLoadScan(command, proxyVmId, proxyVmName, proxyManagementIp, cmdPort);
    }
}
