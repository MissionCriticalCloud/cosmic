package com.cloud.vm;

public class VmWorkDetachVolume extends VmWork {
    private final Long volumeId;

    public VmWorkDetachVolume(final long userId, final long accountId, final long vmId, final String handlerName, final Long volumeId) {
        super(userId, accountId, vmId, handlerName);
        this.volumeId = volumeId;
    }

    public Long getVolumeId() {
        return volumeId;
    }
}
