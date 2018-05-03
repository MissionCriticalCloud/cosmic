package com.cloud.legacymodel.storage;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;

public enum StorageProvisioningType {
    THIN("thin"),
    SPARSE("sparse"),
    FAT("fat");

    private final String provisionType;

    StorageProvisioningType(final String provisionType) {
        this.provisionType = provisionType;
    }

    public static StorageProvisioningType getProvisioningType(final String provisioningType) {

        if (provisioningType.equals(THIN.provisionType)) {
            return StorageProvisioningType.THIN;
        } else if (provisioningType.equals(SPARSE.provisionType)) {
            return StorageProvisioningType.SPARSE;
        } else if (provisioningType.equals(FAT.provisionType)) {
            return StorageProvisioningType.FAT;
        } else {
            throw new CloudRuntimeException("StorageProvisioningType: Unknown provisioning type: " + provisioningType);
        }
    }

    @Override
    public String toString() {
        return provisionType;
    }
}
