package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.UpdateLogicalSwitchPortAnswer;
import com.cloud.legacymodel.communication.command.UpdateLogicalSwitchPortCommand;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.nicira.NiciraNvpTag;
import com.cloud.network.nicira.VifAttachment;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.resource.NiciraNvpUtilities;
import com.cloud.network.utils.CommandRetryUtility;

import java.util.ArrayList;
import java.util.List;

@ResourceWrapper(handles = UpdateLogicalSwitchPortCommand.class)
public final class NiciraNvpUpdateLogicalSwitchPortCommandWrapper extends CommandWrapper<UpdateLogicalSwitchPortCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final UpdateLogicalSwitchPortCommand command, final NiciraNvpResource niciraNvpResource) {
        final NiciraNvpUtilities niciraNvpUtilities = niciraNvpResource.getNiciraNvpUtilities();

        final String logicalSwitchUuid = command.getLogicalSwitchUuid();
        final String logicalSwitchPortUuid = command.getLogicalSwitchPortUuid();
        final String attachmentUuid = command.getAttachmentUuid();

        final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();

        try {
            // Tags set to scope cs_account and account name
            final List<NiciraNvpTag> tags = new ArrayList<>();
            tags.add(new NiciraNvpTag("cs_account", command.getOwnerName()));

            final VifAttachment vifAttachment = niciraNvpUtilities.createVifAttachment(attachmentUuid);

            niciraNvpApi.updateLogicalSwitchPortAttachment(logicalSwitchUuid, logicalSwitchPortUuid, vifAttachment);
            return new UpdateLogicalSwitchPortAnswer(command, true, "Attachment for  " + logicalSwitchPortUuid + " updated", logicalSwitchPortUuid);
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, UpdateLogicalSwitchPortAnswer.class, e);
        }
    }
}
