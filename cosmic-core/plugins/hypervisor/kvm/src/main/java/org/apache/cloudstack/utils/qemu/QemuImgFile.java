package org.apache.cloudstack.utils.qemu;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

public class QemuImgFile {

    private long size = 0;
    private String fileName;
    private PhysicalDiskFormat format = PhysicalDiskFormat.RAW;

    public QemuImgFile(final String fileName) {
        this.fileName = fileName;
    }

    public QemuImgFile(final String fileName, final long size) {
        this.fileName = fileName;
        this.size = size;
    }

    public QemuImgFile(final String fileName, final long size, final PhysicalDiskFormat format) {
        this.fileName = fileName;
        this.size = size;
        this.format = format;
    }

    public QemuImgFile(final String fileName, final PhysicalDiskFormat format) {
        this.fileName = fileName;
        this.format = format;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public PhysicalDiskFormat getFormat() {
        return format;
    }

    public void setFormat(final PhysicalDiskFormat format) {
        this.format = format;
    }
}
