package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.DeleteLogicalRouterAnswer;
import com.cloud.legacymodel.communication.command.DeleteLogicalRouterCommand;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.utils.CommandRetryUtility;

@ResourceWrapper(handles = DeleteLogicalRouterCommand.class)
public final class NiciraNvpDeleteLogicalRouterCommandWrapper extends CommandWrapper<DeleteLogicalRouterCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final DeleteLogicalRouterCommand command, final NiciraNvpResource niciraNvpResource) {
        final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();

        try {
            niciraNvpApi.deleteLogicalRouter(command.getLogicalRouterUuid());
            return new DeleteLogicalRouterAnswer(command, true, "Logical Router deleted (uuid " + command.getLogicalRouterUuid() + ")");
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, DeleteLogicalRouterAnswer.class, e);
        }
    }
}
