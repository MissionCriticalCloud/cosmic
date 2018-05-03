package com.cloud.storage;

import com.cloud.model.enumeration.StoragePoolType;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    public static List<StoragePoolType> getNonSharedStoragePoolTypes() {
        final List<StoragePoolType> nonSharedStoragePoolTypes = new ArrayList<>();
        for (final StoragePoolType storagePoolType : StoragePoolType.values()) {
            if (!storagePoolType.isShared()) {
                nonSharedStoragePoolTypes.add(storagePoolType);
            }
        }
        return nonSharedStoragePoolTypes;
    }
}
