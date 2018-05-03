package com.cloud.consoleproxy;

import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupProxyCommand;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.command.ConsoleAccessAuthenticationCommand;
import com.cloud.legacymodel.communication.command.ConsoleProxyLoadReportCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;

public interface AgentHook {
    void onLoadReport(ConsoleProxyLoadReportCommand cmd);

    AgentControlAnswer onConsoleAccessAuthentication(ConsoleAccessAuthenticationCommand cmd);

    void onAgentConnect(Host host, StartupCommand cmd);

    public void onAgentDisconnect(long agentId, HostStatus state);

    public void startAgentHttpHandlerInVM(StartupProxyCommand startupCmd);
}
