package com.cloud.vm;

public class VmWorkResizeVolume extends VmWork {
    private static final long serialVersionUID = 6112366316907642498L;

    private final long volumeId;
    private final long currentSize;
    private final long newSize;
    private final Long newMinIops;
    private final Long newMaxIops;
    private final Long newServiceOfferingId;
    private final boolean shrinkOk;

    public VmWorkResizeVolume(final long userId, final long accountId, final long vmId, final String handlerName,
                              final long volumeId, final long currentSize, final long newSize, final Long newMinIops, final Long newMaxIops, final Long newServiceOfferingId,
                              final boolean shrinkOk) {

        super(userId, accountId, vmId, handlerName);

        this.volumeId = volumeId;
        this.currentSize = currentSize;
        this.newSize = newSize;
        this.newMinIops = newMinIops;
        this.newMaxIops = newMaxIops;
        this.newServiceOfferingId = newServiceOfferingId;
        this.shrinkOk = shrinkOk;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public long getNewSize() {
        return newSize;
    }

    public Long getNewMinIops() {
        return newMinIops;
    }

    public Long getNewMaxIops() {
        return newMaxIops;
    }

    public Long getNewServiceOfferingId() {
        return newServiceOfferingId;
    }

    public boolean isShrinkOk() {
        return shrinkOk;
    }
}
