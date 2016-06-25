//

//

package com.cloud.network.resource;

import com.cloud.agent.api.CreateLogicalSwitchPortCommand;
import com.cloud.network.nicira.LogicalSwitch;
import com.cloud.network.nicira.LogicalSwitchPort;
import com.cloud.network.nicira.NiciraNvpTag;
import com.cloud.network.nicira.VifAttachment;

import java.util.ArrayList;
import java.util.List;

public class NiciraNvpUtilities {

    private static final NiciraNvpUtilities instance;

    static {
        instance = new NiciraNvpUtilities();
    }

    private NiciraNvpUtilities() {
    }

    public static NiciraNvpUtilities getInstance() {
        return instance;
    }

    public LogicalSwitch createLogicalSwitch() {
        final LogicalSwitch logicalSwitch = new LogicalSwitch();
        return logicalSwitch;
    }

    public LogicalSwitchPort createLogicalSwitchPort(final CreateLogicalSwitchPortCommand command) {
        final String attachmentUuid = command.getAttachmentUuid();

        // Tags set to scope cs_account and account name
        final List<NiciraNvpTag> tags = new ArrayList<>();
        tags.add(new NiciraNvpTag("cs_account", command.getOwnerName()));

        final LogicalSwitchPort logicalSwitchPort = new LogicalSwitchPort(attachmentUuid, tags, true);
        return logicalSwitchPort;
    }

    public VifAttachment createVifAttachment(final String attachmentUuid) {
        return new VifAttachment(attachmentUuid);
    }
}
