package com.cloud.hypervisor.kvm.storage;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

public class KvmPhysicalDisk {
  private String path;
  private final String name;
  private final KvmStoragePool pool;

  public static String rbdStringBuilder(String monHost, int monPort, String authUserName, String authSecret,
      String image) {
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

  private PhysicalDiskFormat format;
  private long size;
  private long virtualSize;

  public KvmPhysicalDisk(String path, String name, KvmStoragePool pool) {
    this.path = path;
    this.name = name;
    this.pool = pool;
  }

  public void setFormat(PhysicalDiskFormat format) {
    this.format = format;
  }

  public PhysicalDiskFormat getFormat() {
    return format;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public long getSize() {
    return size;
  }

  public void setVirtualSize(long size) {
    virtualSize = size;
  }

  public long getVirtualSize() {
    return virtualSize;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public KvmStoragePool getPool() {
    return pool;
  }

  public void setPath(String path) {
    this.path = path;
  }

}
