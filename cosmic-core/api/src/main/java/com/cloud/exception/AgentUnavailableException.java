package com.cloud.exception;

import com.cloud.host.Host;
import com.cloud.utils.SerialVersionUID;

/**
 * command.
 */
public class AgentUnavailableException extends ResourceUnavailableException {

    private static final long serialVersionUID = SerialVersionUID.AgentUnavailableException;

    public AgentUnavailableException(final long agentId) {
        this("Unable to reach host.", agentId);
    }

    public AgentUnavailableException(final String msg, final long agentId) {
        this(msg, agentId, null);
    }

    public AgentUnavailableException(final String msg, final long agentId, final Throwable cause) {
        super("Host " + agentId + ": " + msg, Host.class, agentId, cause);
    }
}
