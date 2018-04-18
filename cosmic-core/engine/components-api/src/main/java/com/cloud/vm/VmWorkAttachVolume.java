package com.cloud.vm;

import com.cloud.model.enumeration.DiskControllerType;

public class VmWorkAttachVolume extends VmWork {
    private static final long serialVersionUID = 553291814854451740L;

    private final Long volumeId;
    private final Long deviceId;
    private final DiskControllerType diskController;

    public VmWorkAttachVolume(final long userId, final long accountId, final long vmId, final String handlerName, final Long volumeId, final Long deviceId,
                              final DiskControllerType diskController) {
        super(userId, accountId, vmId, handlerName);
        this.volumeId = volumeId;
        this.deviceId = deviceId;
        this.diskController = diskController;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public DiskControllerType getDiskController() {
        return diskController;
    }
}
