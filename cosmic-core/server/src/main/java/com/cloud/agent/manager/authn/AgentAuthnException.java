package com.cloud.agent.manager.authn;

import com.cloud.exception.CloudException;

/**
 * Exception indicates authentication and/OR authorization problem
 */
public class AgentAuthnException extends CloudException {

    private static final long serialVersionUID = 3303508953403051189L;

    public AgentAuthnException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AgentAuthnException(final String message) {
        super(message);
    }
}
