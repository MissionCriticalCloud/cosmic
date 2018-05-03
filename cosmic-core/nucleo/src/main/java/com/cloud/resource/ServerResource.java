package com.cloud.resource;

import com.cloud.common.agent.IAgentControl;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.PingCommand;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.model.enumeration.HostType;
import com.cloud.utils.component.Manager;

/**
 * ServerResource is a generic container to execute commands sent
 */
public interface ServerResource extends Manager {
    /**
     * @return Host.Type type of the computing server we have.
     */
    HostType getType();

    /**
     * Generate a startup command containing information regarding the resource.
     *
     * @return StartupCommand ready to be sent to the management server.
     */
    StartupCommand[] initialize();

    /**
     * @param id id of the server to put in the PingCommand
     * @return PingCommand
     */
    PingCommand getCurrentStatus(long id);

    /**
     * Execute the request coming from the computing server.
     *
     * @param cmd Command to execute.
     * @return Answer
     */
    Answer executeRequest(Command cmd);

    /**
     * disconnected() is called when the connection is down between the
     * agent and the management server.  If there are any cleanups, this
     * is the time to do it.
     */
    void disconnected();

    /**
     * This is added to allow calling agent control service from within the resource
     *
     * @return
     */
    IAgentControl getAgentControl();

    void setAgentControl(IAgentControl agentControl);
}
