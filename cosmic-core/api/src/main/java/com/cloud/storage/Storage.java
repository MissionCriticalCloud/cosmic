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

    public enum ImageFormat {
        QCOW2(true, true, false, "qcow2"),
        RAW(false, false, false, "raw"),
        VHD(true, true, true, "vhd"),
        ISO(false, false, false, "iso"),
        OVA(true, true, true, "ova"),
        VHDX(true, true, true, "vhdx"),
        VMDK(true, true, false, "vmdk"),
        VDI(true, true, false, "vdi"),
        TAR(false, false, false, "tar"),
        DIR(false, false, false, "dir");

        private final boolean supportThinProvisioning;
        private final boolean supportSparse;
        private final boolean supportSnapshot;
        private final String fileExtension;

        ImageFormat(final boolean supportThinProvisioning, final boolean supportSparse, final boolean supportSnapshot) {
            this.supportThinProvisioning = supportThinProvisioning;
            this.supportSparse = supportSparse;
            this.supportSnapshot = supportSnapshot;
            fileExtension = null;
        }

        ImageFormat(final boolean supportThinProvisioning, final boolean supportSparse, final boolean supportSnapshot, final String fileExtension) {
            this.supportThinProvisioning = supportThinProvisioning;
            this.supportSparse = supportSparse;
            this.supportSnapshot = supportSnapshot;
            this.fileExtension = fileExtension;
        }

        public boolean supportThinProvisioning() {
            return supportThinProvisioning;
        }

        public boolean supportsSparse() {
            return supportSparse;
        }

        public boolean supportSnapshot() {
            return supportSnapshot;
        }

        public String getFileExtension() {
            if (fileExtension == null) {
                return toString().toLowerCase();
            }

            return fileExtension;
        }

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

    public enum FileSystem {
        Unknown, ext3, ntfs, fat, fat32, ext2, ext4, cdfs, hpfs, ufs, hfs, hfsp
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
