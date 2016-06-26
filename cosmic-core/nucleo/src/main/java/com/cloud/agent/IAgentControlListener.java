//

//

package com.cloud.agent;

import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.transport.Request;
import com.cloud.agent.transport.Response;

public interface IAgentControlListener {
    public Answer processControlRequest(Request request, AgentControlCommand cmd);

    public void processControlResponse(Response response, AgentControlAnswer answer);
}
