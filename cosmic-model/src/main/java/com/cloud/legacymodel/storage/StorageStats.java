package com.cloud.legacymodel.storage;

public interface StorageStats {
    long getByteUsed();

    long getCapacityBytes();
}
