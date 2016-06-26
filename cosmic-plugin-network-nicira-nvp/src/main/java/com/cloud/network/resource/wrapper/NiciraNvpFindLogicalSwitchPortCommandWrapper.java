//

//

package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.FindLogicalSwitchPortAnswer;
import com.cloud.agent.api.FindLogicalSwitchPortCommand;
import com.cloud.network.nicira.LogicalSwitchPort;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.List;

@ResourceWrapper(handles = FindLogicalSwitchPortCommand.class)
public final class NiciraNvpFindLogicalSwitchPortCommandWrapper extends CommandWrapper<FindLogicalSwitchPortCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final FindLogicalSwitchPortCommand command, final NiciraNvpResource niciraNvpResource) {
        final String logicalSwitchUuid = command.getLogicalSwitchUuid();
        final String logicalSwitchPortUuid = command.getLogicalSwitchPortUuid();

        final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();

        try {
            final List<LogicalSwitchPort> ports = niciraNvpApi.findLogicalSwitchPortsByUuid(logicalSwitchUuid, logicalSwitchPortUuid);
            if (ports.size() == 0) {
                return new FindLogicalSwitchPortAnswer(command, false, "Logical switchport " + logicalSwitchPortUuid + " not found", null);
            } else {
                return new FindLogicalSwitchPortAnswer(command, true, "Logical switchport " + logicalSwitchPortUuid + " found", logicalSwitchPortUuid);
            }
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, FindLogicalSwitchPortAnswer.class, e);
        }
    }
}
