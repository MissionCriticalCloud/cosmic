package com.cloud.common.agent;

import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.command.agentcontrolcommand.AgentControlCommand;
import com.cloud.legacymodel.exceptions.AgentControlChannelException;

public interface IAgentControl {
    void registerControlListener(IAgentControlListener listener);

    void unregisterControlListener(IAgentControlListener listener);

    AgentControlAnswer sendRequest(AgentControlCommand cmd, int timeoutInMilliseconds) throws AgentControlChannelException;

    void postRequest(AgentControlCommand cmd) throws AgentControlChannelException;
}
