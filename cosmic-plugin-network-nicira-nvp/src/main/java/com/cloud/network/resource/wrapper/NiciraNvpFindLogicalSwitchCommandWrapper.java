//

//

package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.FindLogicalSwitchAnswer;
import com.cloud.agent.api.FindLogicalSwitchCommand;
import com.cloud.network.nicira.LogicalSwitch;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.List;

@ResourceWrapper(handles = FindLogicalSwitchCommand.class)
public final class NiciraNvpFindLogicalSwitchCommandWrapper extends CommandWrapper<FindLogicalSwitchCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final FindLogicalSwitchCommand command, final NiciraNvpResource niciraNvpResource) {
        final String logicalSwitchUuid = command.getLogicalSwitchUuid();

        final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();

        try {
            final List<LogicalSwitch> switches = niciraNvpApi.findLogicalSwitch(logicalSwitchUuid);
            if (switches.size() == 0) {
                return new FindLogicalSwitchAnswer(command, false, "Logical switc " + logicalSwitchUuid + " not found", null);
            } else {
                return new FindLogicalSwitchAnswer(command, true, "Logical switch " + logicalSwitchUuid + " found", logicalSwitchUuid);
            }
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, FindLogicalSwitchAnswer.class, e);
        }
    }
}
