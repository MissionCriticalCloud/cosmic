package com.cloud.model.enumeration;

public enum StoragePoolType {
    CLVM(true),
    EXT(false),
    Filesystem(false),
    Gluster(true),
    Iscsi(true),
    IscsiLUN(true),
    ISO(false),
    LVM(false),
    ManagedNFS(true),
    NetworkFilesystem(true),
    OCFS2(true),
    PreSetup(true),
    RBD(true),
    SharedMountPoint(true),
    SMB(true);

    boolean shared;

    StoragePoolType(final boolean shared) {
        this.shared = shared;
    }

    public boolean isShared() {
        return shared;
    }
}
