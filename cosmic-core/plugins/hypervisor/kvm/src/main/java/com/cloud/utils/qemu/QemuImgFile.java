package com.cloud.utils.qemu;

public class QemuImgFile {

    private long size = 0;
    private String fileName;
    private QemuImg.PhysicalDiskFormat format = QemuImg.PhysicalDiskFormat.RAW;

    public QemuImgFile(final String fileName) {
        this.fileName = fileName;
    }

    public QemuImgFile(final String fileName, final long size) {
        this.fileName = fileName;
        this.size = size;
    }

    public QemuImgFile(final String fileName, final long size, final QemuImg.PhysicalDiskFormat format) {
        this.fileName = fileName;
        this.size = size;
        this.format = format;
    }

    public QemuImgFile(final String fileName, final QemuImg.PhysicalDiskFormat format) {
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

    public QemuImg.PhysicalDiskFormat getFormat() {
        return format;
    }

    public void setFormat(final QemuImg.PhysicalDiskFormat format) {
        this.format = format;
    }
}
