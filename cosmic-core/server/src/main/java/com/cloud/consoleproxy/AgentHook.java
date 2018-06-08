package com.cloud.consoleproxy;

import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.command.agentcontrol.ConsoleAccessAuthenticationCommand;
import com.cloud.legacymodel.communication.command.agentcontrol.ConsoleProxyLoadReportCommand;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.communication.command.startup.StartupProxyCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;

public interface AgentHook {
    void onLoadReport(ConsoleProxyLoadReportCommand cmd);

    AgentControlAnswer onConsoleAccessAuthentication(ConsoleAccessAuthenticationCommand cmd);

    void onAgentConnect(Host host, StartupCommand cmd);

    public void onAgentDisconnect(long agentId, HostStatus state);

    public void startAgentHttpHandlerInVM(StartupProxyCommand startupCmd);
}
