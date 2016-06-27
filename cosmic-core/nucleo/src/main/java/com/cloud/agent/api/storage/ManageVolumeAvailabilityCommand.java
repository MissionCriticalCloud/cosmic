//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;

public class ManageVolumeAvailabilityCommand extends Command {

    boolean attach;
    String primaryStorageSRUuid;
    String volumeUuid;

    public ManageVolumeAvailabilityCommand() {
    }

    public ManageVolumeAvailabilityCommand(final boolean attach, final String primaryStorageSRUuid, final String volumeUuid) {
        this.attach = attach;
        this.primaryStorageSRUuid = primaryStorageSRUuid;
        this.volumeUuid = volumeUuid;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public boolean getAttach() {
        return attach;
    }

    public String getPrimaryStorageSRUuid() {
        return primaryStorageSRUuid;
    }

    public String getVolumeUuid() {
        return volumeUuid;
    }
}
