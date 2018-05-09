package com.cloud.model.enumeration;

public enum DiskControllerType {
    IDE,
    VIRTIO,
    SCSI;

    public static DiskControllerType getGuestDiskModel(final String platformEmulator) {
        if (platformEmulator == null || platformEmulator.toLowerCase().contains("Non-VirtIO".toLowerCase())) {
            return DiskControllerType.IDE;
        } else if (platformEmulator.toLowerCase().contains("VirtIO-SCSI".toLowerCase())) {
            return DiskControllerType.SCSI;
        } else {
            return DiskControllerType.VIRTIO;
        }
    }
}
