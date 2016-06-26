package com.cloud.storage;

public interface VolumeStats {
    /**
     * @return bytes used by the volume
     */
    public long getBytesUsed();
}
