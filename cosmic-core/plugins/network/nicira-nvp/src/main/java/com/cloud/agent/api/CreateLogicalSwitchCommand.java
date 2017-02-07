package com.cloud.agent.api;

public class CreateLogicalSwitchCommand extends Command {

    private final String transportUuid;
    private final String transportType;
    private final String name;
    private final String ownerName;
    private transient final Long vni;

    public CreateLogicalSwitchCommand(final String transportUuid, final String transportType, final String name,
                                      final String ownerName, final Long vni) {
        this.transportUuid = transportUuid;
        this.transportType = transportType;
        this.name = name;
        this.ownerName = ownerName;
        this.vni = vni;
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

    public Long getVni() {
        return vni;
    }
}
