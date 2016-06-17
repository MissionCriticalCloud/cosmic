package org.apache.cloudstack.utils.qemu;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

public class QemuImgFile {

  private long size = 0;
  private String fileName;
  private PhysicalDiskFormat format = PhysicalDiskFormat.RAW;

  public QemuImgFile(String fileName) {
    this.fileName = fileName;
  }

  public QemuImgFile(String fileName, long size) {
    this.fileName = fileName;
    this.size = size;
  }

  public QemuImgFile(String fileName, long size, PhysicalDiskFormat format) {
    this.fileName = fileName;
    this.size = size;
    this.format = format;
  }

  public QemuImgFile(String fileName, PhysicalDiskFormat format) {
    this.fileName = fileName;
    this.format = format;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public void setFormat(PhysicalDiskFormat format) {
    this.format = format;
  }

  public String getFileName() {
    return fileName;
  }

  public long getSize() {
    return size;
  }

  public PhysicalDiskFormat getFormat() {
    return format;
  }

}