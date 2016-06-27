package com.cloud.storage;

public interface StorageStats {
    /**
     * @return bytes used by the storage server already.
     */
    public long getByteUsed();

    /**
     * @return bytes capacity of the storage server
     */
    public long getCapacityBytes();
}
