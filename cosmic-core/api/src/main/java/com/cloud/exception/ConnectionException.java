package com.cloud.exception;

import com.cloud.utils.SerialVersionUID;

/**
 * ConnectionException is thrown by Listeners while processing the startup
 * command.  There are two uses for this exception and they are distinguished
 * 1. If the flag is set to true, there is an unexpected error during the
 * processing.  Upon receiving this exception, the AgentManager will
 * immediately place the agent under alert.  When the function to enable
 * to disable the agent, the agent is disabled.
 * should be disconnected and reconnected to "refresh" all resource
 * information.  This is useful when the Listener needed to perform setup
 * on the agent and decided it is best to flush connection and reconnect.
 * situation where it keeps throwing ConnectionException.
 */
public class ConnectionException extends CloudException {

    private static final long serialVersionUID = SerialVersionUID.ConnectionException;
    boolean _error;

    public ConnectionException(final boolean setupError, final String msg) {
        this(setupError, msg, null);
    }

    public ConnectionException(final boolean setupError, final String msg, final Throwable cause) {
        super(msg, cause);
        _error = setupError;
    }

    public boolean isSetupError() {
        return _error;
    }
}
