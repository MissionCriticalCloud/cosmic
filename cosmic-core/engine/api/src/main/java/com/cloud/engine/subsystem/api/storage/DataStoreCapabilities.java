package com.cloud.engine.subsystem.api.storage;

public enum DataStoreCapabilities {
    VOLUME_SNAPSHOT_QUIESCEVM,
    STORAGE_SYSTEM_SNAPSHOT // indicates to the StorageSystemSnapshotStrategy that this driver takes snapshots on its own system
}
