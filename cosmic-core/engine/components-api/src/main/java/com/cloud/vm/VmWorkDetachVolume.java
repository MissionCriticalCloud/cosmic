package com.cloud.vm;

public class VmWorkDetachVolume extends VmWork {
    private static final long serialVersionUID = -8722243207385263101L;

    private final Long volumeId;

    public VmWorkDetachVolume(final long userId, final long accountId, final long vmId, final String handlerName, final Long volumeId) {
        super(userId, accountId, vmId, handlerName);
        this.volumeId = volumeId;
    }

    public Long getVolumeId() {
        return volumeId;
    }
}
