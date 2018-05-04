package com.cloud.network.element;

import com.cloud.deploy.DeployDestination;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.Network;

public interface AggregatedCommandExecutor {
    public boolean prepareAggregatedExecution(Network network, DeployDestination dest) throws ResourceUnavailableException;

    public boolean completeAggregatedExecution(Network network, DeployDestination dest) throws ResourceUnavailableException;

    public boolean cleanupAggregatedExecution(Network network, DeployDestination dest) throws ResourceUnavailableException;
}
