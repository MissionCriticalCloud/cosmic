package com.cloud.cluster;

import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.model.enumeration.Event;

public interface ClusteredAgentRebalanceService {
    int DEFAULT_TRANSFER_CHECK_INTERVAL = 10000;

    void scheduleRebalanceAgents();

    boolean executeRebalanceRequest(long agentId, long currentOwnerId, long futureOwnerId, Event event) throws AgentUnavailableException, OperationTimedoutException;
}
