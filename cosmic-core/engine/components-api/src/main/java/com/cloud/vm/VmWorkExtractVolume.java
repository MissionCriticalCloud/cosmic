package com.cloud.vm;

public class VmWorkExtractVolume extends VmWork {
    private final long volumeId;
    private final long zoneId;

    public VmWorkExtractVolume(final long userId, final long accountId, final long vmId, final String handlerName, final long volumeId, final long zoneId) {
        super(userId, accountId, vmId, handlerName);
        this.volumeId = volumeId;
        this.zoneId = zoneId;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public long getZoneId() {
        return zoneId;
    }
}
