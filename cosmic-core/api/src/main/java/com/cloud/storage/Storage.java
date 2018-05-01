package com.cloud.storage;

import com.cloud.model.enumeration.StoragePoolType;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

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

    public enum ProvisioningType {
        THIN("thin"),
        SPARSE("sparse"),
        FAT("fat");

        private final String provisionType;

        ProvisioningType(final String provisionType) {
            this.provisionType = provisionType;
        }

        public static ProvisioningType getProvisioningType(final String provisioningType) {

            if (provisioningType.equals(THIN.provisionType)) {
                return ProvisioningType.THIN;
            } else if (provisioningType.equals(SPARSE.provisionType)) {
                return ProvisioningType.SPARSE;
            } else if (provisioningType.equals(FAT.provisionType)) {
                return ProvisioningType.FAT;
            } else {
                throw new NotImplementedException();
            }
        }

        @Override
        public String toString() {
            return provisionType;
        }
    }

    public enum TemplateType {
        ROUTING, // Router template
        SYSTEM, /* routing, system vm template */
        BUILTIN, /* buildin template */
        PERHOST, /* every host has this template, don't need to install it in secondary storage */
        USER /* User supplied template/iso */
    }

    public enum StorageResourceType {
        STORAGE_POOL, STORAGE_HOST, SECONDARY_STORAGE, LOCAL_SECONDARY_STORAGE
    }
}
