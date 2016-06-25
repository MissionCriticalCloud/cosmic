//

//

package com.cloud.network.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.network.nicira.ControlClusterStatus;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CheckHealthCommand.class)
public class NiciraCheckHealthCommandWrapper extends CommandWrapper<CheckHealthCommand, Answer, NiciraNvpResource> {

    private static final String CONTROL_CLUSTER_STATUS_IS_STABLE = "stable";
    private static final Logger s_logger = LoggerFactory.getLogger(NiciraCheckHealthCommandWrapper.class);

    @Override
    public Answer execute(final CheckHealthCommand command, final NiciraNvpResource serverResource) {
        final NiciraNvpApi niciraNvpApi = serverResource.getNiciraNvpApi();
        boolean healthy = true;
        try {
            final ControlClusterStatus clusterStatus = niciraNvpApi.getControlClusterStatus();
            final String status = clusterStatus.getClusterStatus();
            if (clusterIsUnstable(status)) {
                s_logger.warn("Control cluster is not stable. Current status is " + status);
                healthy = false;
            }
        } catch (final NiciraNvpApiException e) {
            s_logger.error("Exception caught while checking control cluster status during health check", e);
            healthy = false;
        }

        return new CheckHealthAnswer(command, healthy);
    }

    protected boolean clusterIsUnstable(final String clusterStatus) {
        return !CONTROL_CLUSTER_STATUS_IS_STABLE.equals(clusterStatus);
    }
}
