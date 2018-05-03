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

    public enum TemplateType {
        ROUTING, // Router template
        SYSTEM, /* routing, system vm template */
        BUILTIN, /* buildin template */
        PERHOST, /* every host has this template, don't need to install it in secondary storage */
        USER /* User supplied template/iso */
    }
}
