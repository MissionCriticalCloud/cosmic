//

//

package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.DeleteLogicalSwitchPortAnswer;
import com.cloud.agent.api.DeleteLogicalSwitchPortCommand;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = DeleteLogicalSwitchPortCommand.class)
public final class NiciraNvpDeleteLogicalSwitchPortCommandWrapper extends CommandWrapper<DeleteLogicalSwitchPortCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final DeleteLogicalSwitchPortCommand command, final NiciraNvpResource niciraNvpResource) {
        final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();

        try {
            niciraNvpApi.deleteLogicalSwitchPort(command.getLogicalSwitchUuid(), command.getLogicalSwitchPortUuid());
            return new DeleteLogicalSwitchPortAnswer(command, true, "Logical switch port " + command.getLogicalSwitchPortUuid() + " deleted");
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, DeleteLogicalSwitchPortAnswer.class, e);
        }
    }
}
