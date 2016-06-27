//

//

package com.cloud.agent;

import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.exception.AgentControlChannelException;

public interface IAgentControl {
    void registerControlListener(IAgentControlListener listener);

    void unregisterControlListener(IAgentControlListener listener);

    AgentControlAnswer sendRequest(AgentControlCommand cmd, int timeoutInMilliseconds) throws AgentControlChannelException;

    void postRequest(AgentControlCommand cmd) throws AgentControlChannelException;
}
