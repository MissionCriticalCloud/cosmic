package com.cloud.vm;

public class VmWorkAttachVolume extends VmWork {
    private static final long serialVersionUID = 553291814854451740L;

    private final Long volumeId;
    private final Long deviceId;

    public VmWorkAttachVolume(final long userId, final long accountId, final long vmId, final String handlerName, final Long volumeId, final Long deviceId) {
        super(userId, accountId, vmId, handlerName);
        this.volumeId = volumeId;
        this.deviceId = deviceId;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    public Long getDeviceId() {
        return deviceId;
    }
}
