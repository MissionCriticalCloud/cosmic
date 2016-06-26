package com.cloud.vm;

public class VmWorkMigrateVolume extends VmWork {
    private static final long serialVersionUID = -565778516928408602L;

    private final long volumeId;
    private final long destPoolId;
    private final boolean liveMigrate;

    public VmWorkMigrateVolume(final long userId, final long accountId, final long vmId, final String handlerName, final long volumeId, final long destPoolId, final boolean
            liveMigrate) {
        super(userId, accountId, vmId, handlerName);
        this.volumeId = volumeId;
        this.destPoolId = destPoolId;
        this.liveMigrate = liveMigrate;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public long getDestPoolId() {
        return destPoolId;
    }

    public boolean isLiveMigrate() {
        return liveMigrate;
    }
}
