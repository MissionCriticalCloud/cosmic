package com.cloud.agent.resource.kvm.storage;

import com.cloud.model.enumeration.ImageFormat;
import com.cloud.utils.qemu.QemuImg.PhysicalDiskFormat;

public class KvmPhysicalDisk {
    private final String name;
    private final KvmStoragePool pool;
    private String path;
    private PhysicalDiskFormat format;
    private long size;
    private long virtualSize;

    public KvmPhysicalDisk(final String path, final String name, final KvmStoragePool pool) {
        this.path = path;
        this.name = name;
        this.pool = pool;
    }

    public static String rbdStringBuilder(final String monHost, final int monPort, final String authUserName, final String authSecret,
                                          final String image) {
        String rbdOpts;

        rbdOpts = "rbd:" + image;
        rbdOpts += ":mon_host=" + monHost;
        if (monPort != 6789) {
            rbdOpts += "\\\\:" + monPort;
        }

        if (authUserName == null) {
            rbdOpts += ":auth_supported=none";
        } else {
            rbdOpts += ":auth_supported=cephx";
            rbdOpts += ":id=" + authUserName;
            rbdOpts += ":key=" + authSecret;
        }

        rbdOpts += ":rbd_default_format=2";
        rbdOpts += ":client_mount_timeout=30";

        return rbdOpts;
    }

    public PhysicalDiskFormat getFormat() {
        return this.format;
    }

    public void setFormat(final PhysicalDiskFormat format) {
        this.format = format;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getVirtualSize() {
        return this.virtualSize;
    }

    public void setVirtualSize(final long size) {
        this.virtualSize = size;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public KvmStoragePool getPool() {
        return this.pool;
    }

    public PhysicalDiskFormat getPhysicalDiskFormatFromImageFormat(final ImageFormat imageFormat) {
        if (imageFormat == null) {
            return PhysicalDiskFormat.QCOW2;
        }
        switch (imageFormat) {
            case RAW:
                return PhysicalDiskFormat.RAW;
            case QCOW2:
                return PhysicalDiskFormat.QCOW2;
            case DIR:
                return PhysicalDiskFormat.DIR;
            case TAR:
                return PhysicalDiskFormat.TAR;
            default:
                return PhysicalDiskFormat.QCOW2;
        }
    }
}
