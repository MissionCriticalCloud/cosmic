package com.cloud.model.enumeration;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public enum PreallocationType {
    Off,
    Metadata,
    Full;

    public static PreallocationType getPreallocationType(final StorageProvisioningType provisioningType) {
        switch (provisioningType) {
            case THIN:
                return PreallocationType.Off;
            case SPARSE:
                return PreallocationType.Metadata;
            case FAT:
                return PreallocationType.Full;
            default:
                throw new NotImplementedException();
        }
    }
}
