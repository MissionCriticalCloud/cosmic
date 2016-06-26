package com.cloud.storage;

public class ResizeVolumePayload {
    public final Long newSize;
    public final Long newMinIops;
    public final Long newMaxIops;
    public final boolean shrinkOk;
    public final String instanceName;
    public final long[] hosts;

    public ResizeVolumePayload(final Long newSize, final Long newMinIops, final Long newMaxIops, final boolean shrinkOk, final String instanceName, final long[] hosts) {
        this.newSize = newSize;
        this.newMinIops = newMinIops;
        this.newMaxIops = newMaxIops;
        this.shrinkOk = shrinkOk;
        this.instanceName = instanceName;
        this.hosts = hosts;
    }
}
