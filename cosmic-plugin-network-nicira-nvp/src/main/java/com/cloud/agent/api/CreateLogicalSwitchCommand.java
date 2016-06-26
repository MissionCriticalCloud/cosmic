//

//

package com.cloud.agent.api;

public class CreateLogicalSwitchCommand extends Command {

    private final String transportUuid;
    private final String transportType;
    private final String name;
    private final String ownerName;

    public CreateLogicalSwitchCommand(final String transportUuid, final String transportType, final String name, final String ownerName) {
        this.transportUuid = transportUuid;
        this.transportType = transportType;
        this.name = name;
        this.ownerName = ownerName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getTransportUuid() {
        return transportUuid;
    }

    public String getTransportType() {
        return transportType;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
