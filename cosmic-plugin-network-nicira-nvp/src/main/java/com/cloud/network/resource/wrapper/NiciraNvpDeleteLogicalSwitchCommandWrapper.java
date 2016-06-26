//

//

package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.DeleteLogicalSwitchAnswer;
import com.cloud.agent.api.DeleteLogicalSwitchCommand;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = DeleteLogicalSwitchCommand.class)
public final class NiciraNvpDeleteLogicalSwitchCommandWrapper extends CommandWrapper<DeleteLogicalSwitchCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final DeleteLogicalSwitchCommand command, final NiciraNvpResource niciraNvpResource) {
        try {
            final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();
            niciraNvpApi.deleteLogicalSwitch(command.getLogicalSwitchUuid());
            return new DeleteLogicalSwitchAnswer(command, true, "Logicalswitch " + command.getLogicalSwitchUuid() + " deleted");
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, DeleteLogicalSwitchAnswer.class, e);
        }
    }
}
