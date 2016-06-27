//

//

package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ConfigurePublicIpsOnLogicalRouterAnswer;
import com.cloud.agent.api.ConfigurePublicIpsOnLogicalRouterCommand;
import com.cloud.network.nicira.LogicalRouterPort;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.List;

@ResourceWrapper(handles = ConfigurePublicIpsOnLogicalRouterCommand.class)
public final class NiciraNvpConfigurePublicIpsCommandWrapper extends CommandWrapper<ConfigurePublicIpsOnLogicalRouterCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final ConfigurePublicIpsOnLogicalRouterCommand command, final NiciraNvpResource niciraNvpResource) {
        final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();

        try {
            final List<LogicalRouterPort> ports = niciraNvpApi.findLogicalRouterPortByGatewayServiceUuid(command.getLogicalRouterUuid(), command.getL3GatewayServiceUuid());
            if (ports.size() != 1) {
                return new ConfigurePublicIpsOnLogicalRouterAnswer(command, false, "No logical router ports found, unable to set ip addresses");
            }
            final LogicalRouterPort lrp = ports.get(0);
            lrp.setIpAddresses(command.getPublicCidrs());
            niciraNvpApi.updateLogicalRouterPort(command.getLogicalRouterUuid(), lrp);

            return new ConfigurePublicIpsOnLogicalRouterAnswer(command, true, "Configured " + command.getPublicCidrs().size() + " ip addresses on logical router uuid " +
                    command.getLogicalRouterUuid());
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, ConfigurePublicIpsOnLogicalRouterAnswer.class, e);
        }
    }
}
