package com.cloud.exception;

import com.cloud.agent.api.Command;
import com.cloud.utils.SerialVersionUID;

/**
 * wait timeout.
 */
public class OperationTimedoutException extends CloudException {
    private static final long serialVersionUID = SerialVersionUID.OperationTimedoutException;

    long _agentId;
    long _seqId;
    int _time;

    // TODO
    // I did a reference search on usage of getCommands() and found none
    //
    // to prevent serialization problems across boundaries, I'm disabling serialization of _cmds here
    // getCommands() will still be available within the same serialization boundary, but it will be lost
    // when exception is propagated across job boundaries.
    //
    transient Command[] _cmds;
    boolean _isActive;

    public OperationTimedoutException(final Command[] cmds, final long agentId, final long seqId, final int time, final boolean isActive) {
        super("Commands " + seqId + " to Host " + agentId + " timed out after " + time);
        _agentId = agentId;
        _seqId = seqId;
        _time = time;
        _cmds = cmds;
        _isActive = isActive;
    }

    public long getAgentId() {
        return _agentId;
    }

    public long getSequenceId() {
        return _seqId;
    }

    public int getWaitTime() {
        return _time;
    }

    public Command[] getCommands() {
        return _cmds;
    }

    public boolean isActive() {
        return _isActive;
    }
}
