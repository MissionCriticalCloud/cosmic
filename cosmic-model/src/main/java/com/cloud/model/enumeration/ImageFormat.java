package com.cloud.model.enumeration;

public enum ImageFormat {
    QCOW2("qcow2"),
    RAW("raw"),
    VHD("vhd"),
    ISO("iso"),
    OVA("ova"),
    VHDX("vhdx"),
    VMDK("vmdk"),
    VDI("vdi"),
    TAR("tar"),
    DIR("dir");

    private final String fileExtension;

    ImageFormat(final String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        if (fileExtension == null) {
            return toString().toLowerCase();
        }

        return fileExtension;
    }
}
