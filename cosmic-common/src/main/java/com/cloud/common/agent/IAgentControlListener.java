package com.cloud.common.agent;

import com.cloud.common.transport.Request;
import com.cloud.common.transport.Response;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.agentcontrolcommand.AgentControlCommand;

public interface IAgentControlListener {
    Answer processControlRequest(Request request, AgentControlCommand cmd);

    void processControlResponse(Response response, AgentControlAnswer answer);
}
