package com.cloud.agent.resource;

import com.cloud.common.agent.IAgentControl;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.model.enumeration.HostType;

import javax.naming.ConfigurationException;
import java.util.Map;

public interface AgentResource {
    String getName();

    void setName(String name);

    boolean configure(Map<String, Object> params) throws ConfigurationException;

    boolean start();

    boolean stop();

    HostType getType();

    StartupCommand[] initialize();

    PingCommand getCurrentStatus(long id);

    Answer executeRequest(Command cmd);

    void disconnected();

    IAgentControl getAgentControl();

    void setAgentControl(IAgentControl agentControl);
}
