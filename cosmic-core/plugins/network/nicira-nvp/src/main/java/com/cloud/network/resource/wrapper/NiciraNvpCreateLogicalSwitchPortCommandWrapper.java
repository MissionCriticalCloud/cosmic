//

//

package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateLogicalSwitchPortAnswer;
import com.cloud.agent.api.CreateLogicalSwitchPortCommand;
import com.cloud.network.nicira.LogicalSwitchPort;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.nicira.VifAttachment;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.resource.NiciraNvpUtilities;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CreateLogicalSwitchPortCommand.class)
public final class NiciraNvpCreateLogicalSwitchPortCommandWrapper extends CommandWrapper<CreateLogicalSwitchPortCommand, Answer, NiciraNvpResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(NiciraNvpCreateLogicalSwitchPortCommandWrapper.class);

    @Override
    public Answer execute(final CreateLogicalSwitchPortCommand command, final NiciraNvpResource niciraNvpResource) {
        final NiciraNvpUtilities niciraNvpUtilities = niciraNvpResource.getNiciraNvpUtilities();

        final String logicalSwitchUuid = command.getLogicalSwitchUuid();
        final String attachmentUuid = command.getAttachmentUuid();

        try {
            final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();

            final LogicalSwitchPort logicalSwitchPort = niciraNvpUtilities.createLogicalSwitchPort(command);
            final LogicalSwitchPort newPort = niciraNvpApi.createLogicalSwitchPort(logicalSwitchUuid, logicalSwitchPort);
            try {
                niciraNvpApi.updateLogicalSwitchPortAttachment(command.getLogicalSwitchUuid(), newPort.getUuid(), new VifAttachment(attachmentUuid));
            } catch (final NiciraNvpApiException ex) {
                s_logger.warn("modifyLogicalSwitchPort failed after switchport was created, removing switchport");
                niciraNvpApi.deleteLogicalSwitchPort(command.getLogicalSwitchUuid(), newPort.getUuid());
                throw ex; // Rethrow the original exception
            }
            return new CreateLogicalSwitchPortAnswer(command, true, "Logical switch port " + newPort.getUuid() + " created", newPort.getUuid());
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, CreateLogicalSwitchPortAnswer.class, e);
        }
    }
}
