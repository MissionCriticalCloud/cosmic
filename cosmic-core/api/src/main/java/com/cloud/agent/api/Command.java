package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * implemented by classes that extends the Command class. Command specifies
 */
public abstract class Command {

    public static final String HYPERVISOR_TYPE = "hypervisorType";
    // allow command to carry over hypervisor or other environment related context info
    @LogLevel(Log4jLevel.Trace)
    protected Map<String, String> contextMap = new HashMap<>();
    private int wait;  //in second

    protected Command() {
        this.wait = 0;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(final int wait) {
        this.wait = wait;
    }

    /**
     * @return Does this command need to be executed in sequence on the agent?
     * When this is set to true, the commands are executed by a single
     * thread on the agent.
     */
    public abstract boolean executeInSequence();

    public void setContextParam(final String name, final String value) {
        contextMap.put(name, value);
    }

    public String getContextParam(final String name) {
        return contextMap.get(name);
    }

    public boolean allowCaching() {
        return true;
    }

    @Override
    public int hashCode() {
        int result = contextMap != null ? contextMap.hashCode() : 0;
        result = 31 * result + wait;
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Command)) {
            return false;
        }

        final Command command = (Command) o;

        if (wait != command.wait) {
            return false;
        }
        if (contextMap != null ? !contextMap.equals(command.contextMap) : command.contextMap != null) {
            return false;
        }

        return true;
    }

    @Override
    public final String toString() {
        return this.getClass().getName();
    }

    public static enum OnError {
        Continue, Stop
    }
}
