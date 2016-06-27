package com.cloud.hypervisor.kvm.storage;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

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
        return format;
    }

    public void setFormat(final PhysicalDiskFormat format) {
        this.format = format;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getVirtualSize() {
        return virtualSize;
    }

    public void setVirtualSize(final long size) {
        virtualSize = size;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public KvmStoragePool getPool() {
        return pool;
    }
}
