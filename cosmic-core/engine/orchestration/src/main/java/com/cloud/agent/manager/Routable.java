package com.cloud.agent.manager;

import com.cloud.exception.AgentUnavailableException;

public interface Routable {
    /**
     * Directly rout this data to the agent.
     *
     * @param data
     * @throws AgentUnavailableException
     */
    void routeToAgent(byte[] data) throws AgentUnavailableException;
}
