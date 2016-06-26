package com.cloud.storage;

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

    public static enum ImageFormat {
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

        private ImageFormat(final boolean supportThinProvisioning, final boolean supportSparse, final boolean supportSnapshot) {
            this.supportThinProvisioning = supportThinProvisioning;
            this.supportSparse = supportSparse;
            this.supportSnapshot = supportSnapshot;
            fileExtension = null;
        }

        private ImageFormat(final boolean supportThinProvisioning, final boolean supportSparse, final boolean supportSnapshot, final String fileExtension) {
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

    public static enum ProvisioningType {
        THIN("thin"),
        SPARSE("sparse"),
        FAT("fat");

        private final String provisionType;

        private ProvisioningType(final String provisionType) {
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

    public static enum FileSystem {
        Unknown, ext3, ntfs, fat, fat32, ext2, ext4, cdfs, hpfs, ufs, hfs, hfsp
    }

    public static enum TemplateType {
        ROUTING, // Router template
        SYSTEM, /* routing, system vm template */
        BUILTIN, /* buildin template */
        PERHOST, /* every host has this template, don't need to install it in secondary storage */
        USER /* User supplied template/iso */
    }

    public static enum StoragePoolType {
        Filesystem(false), // local directory
        NetworkFilesystem(true), // NFS
        IscsiLUN(true), // shared LUN, with a clusterfs overlay
        Iscsi(true), // for e.g., ZFS Comstar
        ISO(false), // for iso image
        LVM(false), // XenServer local LVM SR
        CLVM(true),
        RBD(true), // http://libvirt.org/storage.html#StorageBackendRBD
        SharedMountPoint(true),
        PreSetup(true), // for XenServer, Storage Pool is set up by customers.
        EXT(false), // XenServer local EXT SR
        OCFS2(true),
        SMB(true),
        Gluster(true),
        ManagedNFS(true);

        boolean shared;

        StoragePoolType(final boolean shared) {
            this.shared = shared;
        }

        public boolean isShared() {
            return shared;
        }
    }

    public static enum StorageResourceType {
        STORAGE_POOL, STORAGE_HOST, SECONDARY_STORAGE, LOCAL_SECONDARY_STORAGE
    }
}
