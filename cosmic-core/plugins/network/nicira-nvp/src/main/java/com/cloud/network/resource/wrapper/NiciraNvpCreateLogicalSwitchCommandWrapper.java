//

//

package com.cloud.network.resource.wrapper;

import static com.cloud.network.resource.NiciraNvpResource.NUM_RETRIES;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CreateLogicalSwitchAnswer;
import com.cloud.agent.api.CreateLogicalSwitchCommand;
import com.cloud.network.nicira.LogicalSwitch;
import com.cloud.network.nicira.NiciraNvpApi;
import com.cloud.network.nicira.NiciraNvpApiException;
import com.cloud.network.nicira.NiciraNvpTag;
import com.cloud.network.nicira.TransportZoneBinding;
import com.cloud.network.resource.NiciraNvpResource;
import com.cloud.network.resource.NiciraNvpUtilities;
import com.cloud.network.utils.CommandRetryUtility;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.ArrayList;
import java.util.List;

@ResourceWrapper(handles = CreateLogicalSwitchCommand.class)
public final class NiciraNvpCreateLogicalSwitchCommandWrapper extends CommandWrapper<CreateLogicalSwitchCommand, Answer, NiciraNvpResource> {

    @Override
    public Answer execute(final CreateLogicalSwitchCommand command, final NiciraNvpResource niciraNvpResource) {
        final NiciraNvpUtilities niciraNvpUtilities = niciraNvpResource.getNiciraNvpUtilities();

        LogicalSwitch logicalSwitch = niciraNvpUtilities.createLogicalSwitch();
        logicalSwitch.setDisplayName(niciraNvpResource.truncate("lswitch-" + command.getName(), NiciraNvpResource.NAME_MAX_LEN));
        logicalSwitch.setPortIsolationEnabled(false);

        // Set transport binding
        final List<TransportZoneBinding> ltzb = new ArrayList<>();
        ltzb.add(new TransportZoneBinding(command.getTransportUuid(), command.getTransportType()));
        logicalSwitch.setTransportZones(ltzb);

        // Tags set to scope cs_account and account name
        final List<NiciraNvpTag> tags = new ArrayList<>();
        tags.add(new NiciraNvpTag("cs_account", command.getOwnerName()));
        logicalSwitch.setTags(tags);

        try {
            final NiciraNvpApi niciraNvpApi = niciraNvpResource.getNiciraNvpApi();
            logicalSwitch = niciraNvpApi.createLogicalSwitch(logicalSwitch);
            final String switchUuid = logicalSwitch.getUuid();
            return new CreateLogicalSwitchAnswer(command, true, "Logicalswitch " + switchUuid + " created", switchUuid);
        } catch (final NiciraNvpApiException e) {
            final CommandRetryUtility retryUtility = niciraNvpResource.getRetryUtility();
            retryUtility.addRetry(command, NUM_RETRIES);
            return retryUtility.retry(command, CreateLogicalSwitchAnswer.class, e);
        }
    }
}
