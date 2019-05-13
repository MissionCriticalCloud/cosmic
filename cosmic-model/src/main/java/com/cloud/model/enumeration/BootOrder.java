package com.cloud.model.enumeration;

import java.util.Arrays;

public enum BootOrder {
    HARDDISK("hd"),
    CDROM("cdrom"),
    FLOPPY("fd"),
    NETWORK("network");

    final private String device;

    BootOrder(final String device) {
        this.device = device;
    }

    public static BootOrder stringToEnum(String s) {
        return Arrays.stream(BootOrder.values())
                     .filter(v -> v.device.equals(s))
                     .findFirst()
                     // If we find an undefined device always defaults to HARDDISK
                     .orElse(BootOrder.HARDDISK);
    }

    public String getValue() {
        return this.device;
    }
}

