package com.cloud.consoleproxy;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.ConsoleAccessAuthenticationCommand;
import com.cloud.agent.api.ConsoleProxyLoadReportCommand;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupProxyCommand;
import com.cloud.host.Host;
import com.cloud.host.Status;

public class ConsoleProxyListener implements Listener {
    AgentHook _proxyMgr = null;

    public ConsoleProxyListener(final AgentHook proxyMgr) {
        _proxyMgr = proxyMgr;
    }

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        return true;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] commands) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        if (cmd instanceof ConsoleProxyLoadReportCommand) {
            _proxyMgr.onLoadReport((ConsoleProxyLoadReportCommand) cmd);

            // return dummy answer
            return new AgentControlAnswer(cmd);
        } else if (cmd instanceof ConsoleAccessAuthenticationCommand) {
            return _proxyMgr.onConsoleAccessAuthentication((ConsoleAccessAuthenticationCommand) cmd);
        }
        return null;
    }

    @Override
    public void processConnect(final Host host, final StartupCommand cmd, final boolean forRebalance) {
        _proxyMgr.onAgentConnect(host, cmd);

        if (cmd instanceof StartupProxyCommand) {
            _proxyMgr.startAgentHttpHandlerInVM((StartupProxyCommand) cmd);
        }
    }

    @Override
    public boolean processDisconnect(final long agentId, final Status state) {
        _proxyMgr.onAgentDisconnect(agentId, state);
        return true;
    }

    @Override
    public boolean isRecurring() {
        return true;
    }

    @Override
    public int getTimeout() {
        return -1;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        return true;
    }
}
