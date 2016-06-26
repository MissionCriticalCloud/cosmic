package org.apache.cloudstack.engine.subsystem.api.storage.disktype;

import com.cloud.utils.exception.CloudRuntimeException;

public enum DiskFormat {
    VMDK, VHD, VHDX, ISO, QCOW2;

    public static DiskFormat getFormat(final String format) {
        if (VMDK.toString().equalsIgnoreCase(format)) {
            return VMDK;
        } else if (VHD.toString().equalsIgnoreCase(format)) {
            return VHD;
        } else if (VHDX.toString().equalsIgnoreCase(format)) {
            return VHDX;
        } else if (QCOW2.toString().equalsIgnoreCase(format)) {
            return QCOW2;
        } else if (ISO.toString().equalsIgnoreCase(format)) {
            return ISO;
        }
        throw new CloudRuntimeException("can't find format match: " + format);
    }
}
