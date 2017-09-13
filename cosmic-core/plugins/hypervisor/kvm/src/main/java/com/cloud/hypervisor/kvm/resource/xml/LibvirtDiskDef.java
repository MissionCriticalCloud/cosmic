package com.cloud.hypervisor.kvm.resource.xml;

public class LibvirtDiskDef {
    private DeviceType deviceType; /* floppy, disk, cdrom */
    private DiskType diskType;
    private DiskProtocol diskProtocol;
    private String sourcePath;
    private String sourceHost;
    private int sourcePort;
    private String authUserName;
    private String authSecretUuid;
    private String diskLabel;
    private DiskBus bus;
    private DiskFmtType diskFmtType; /* qcow2, raw etc. */
    private boolean readonly;
    private boolean shareable;
    private boolean deferAttach;
    private Long bytesReadRate;
    private Long bytesWriteRate;
    private Long iopsReadRate;
    private Long iopsWriteRate;
    private DiskCacheMode diskCacheMode;
    private String serial;
    private boolean qemuDriver = true;
    private DiscardType discard = DiscardType.IGNORE;

    public DiscardType getDiscard() {
        return discard;
    }

    public void setDiscard(DiscardType discard) {
        this.discard = discard;
    }

    public void defFileBasedDisk(final String filePath, final String diskLabel, final DiskBus bus, final DiskFmtType diskFmtType) {
        diskType = DiskType.FILE;
        deviceType = DeviceType.DISK;
        diskCacheMode = DiskCacheMode.NONE;
        sourcePath = filePath;
        this.diskLabel = diskLabel;
        this.diskFmtType = diskFmtType;
        this.bus = bus;
    }

    public void defFileBasedDisk(final String filePath, final int devId, final DiskBus bus, final DiskFmtType diskFmtType) {
        diskType = DiskType.FILE;
        deviceType = DeviceType.DISK;
        diskCacheMode = DiskCacheMode.NONE;
        sourcePath = filePath;
        diskLabel = getDevLabel(devId, bus);
        this.diskFmtType = diskFmtType;
        this.bus = bus;
    }

    /* skip iso label */
    private String getDevLabel(int devId, final DiskBus bus) {
        if (devId < 0) {
            return "";
        }

        if (devId == 2) {
            devId++;
        }

        if (bus == DiskBus.SCSI) {
            return "sd" + getDevLabelSuffix(devId);
        } else if (bus == DiskBus.VIRTIO) {
            return "vd" + getDevLabelSuffix(devId);
        }
        return "hd" + getDevLabelSuffix(devId);
    }

    private String getDevLabelSuffix(int deviceIndex) {
        if (deviceIndex < 0) {
            return "";
        }

        int base = 'z' - 'a' + 1;
        String labelSuffix = "";
        do {
            char suffix = (char) ('a' + (deviceIndex % base));
            labelSuffix = suffix + labelSuffix;
            deviceIndex = (deviceIndex / base) - 1;
        } while (deviceIndex >= 0);

        return labelSuffix;
    }

    public void defIsoDisk(final String volPath) {
        diskType = DiskType.FILE;
        deviceType = DeviceType.CDROM;
        sourcePath = volPath;
        diskLabel = "hdc";
        diskFmtType = DiskFmtType.RAW;
        diskCacheMode = DiskCacheMode.NONE;
        bus = DiskBus.IDE;
    }

    public void defBlockBasedDisk(final String diskName, final int devId, final DiskBus bus) {
        diskType = DiskType.BLOCK;
        deviceType = DeviceType.DISK;
        diskFmtType = DiskFmtType.RAW;
        diskCacheMode = DiskCacheMode.NONE;
        sourcePath = diskName;
        diskLabel = getDevLabel(devId, bus);
        this.bus = bus;
    }

    public void defBlockBasedDisk(final String diskName, final String diskLabel, final DiskBus bus) {
        diskType = DiskType.BLOCK;
        deviceType = DeviceType.DISK;
        diskFmtType = DiskFmtType.RAW;
        diskCacheMode = DiskCacheMode.NONE;
        sourcePath = diskName;
        this.diskLabel = diskLabel;
        this.bus = bus;
    }

    public void defNetworkBasedDisk(final String diskName, final String sourceHost, final int sourcePort, final String authUserName,
                                    final String authSecretUuid, final int devId, final DiskBus bus,
                                    final DiskProtocol protocol, final DiskFmtType diskFmtType) {
        diskType = DiskType.NETWORK;
        deviceType = DeviceType.DISK;
        this.diskFmtType = diskFmtType;
        diskCacheMode = DiskCacheMode.NONE;
        sourcePath = diskName;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        this.authUserName = authUserName;
        this.authSecretUuid = authSecretUuid;
        diskLabel = getDevLabel(devId, bus);
        this.bus = bus;
        diskProtocol = protocol;
    }

    public void defNetworkBasedDisk(final String diskName, final String sourceHost, final int sourcePort, final String authUserName,
                                    final String authSecretUuid, final String diskLabel, final DiskBus bus,
                                    final DiskProtocol protocol, final DiskFmtType diskFmtType) {
        diskType = DiskType.NETWORK;
        deviceType = DeviceType.DISK;
        this.diskFmtType = diskFmtType;
        diskCacheMode = DiskCacheMode.NONE;
        sourcePath = diskName;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        this.authUserName = authUserName;
        this.authSecretUuid = authSecretUuid;
        this.diskLabel = diskLabel;
        this.bus = bus;
        diskProtocol = protocol;
    }

    public void setReadonly() {
        readonly = true;
    }

    public void setSharable() {
        shareable = true;
    }

    public boolean isAttachDeferred() {
        return deferAttach;
    }

    public void setAttachDeferred(final boolean deferAttach) {
        this.deferAttach = deferAttach;
    }

    public String getDiskPath() {
        return sourcePath;
    }

    public void setDiskPath(final String volPath) {
        sourcePath = volPath;
    }

    public String getDiskLabel() {
        return diskLabel;
    }

    public DiskType getDiskType() {
        return diskType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(final DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public DiskBus getBusType() {
        return bus;
    }

    public DiskFmtType getDiskFormatType() {
        return diskFmtType;
    }

    public void setBytesReadRate(final Long bytesReadRate) {
        this.bytesReadRate = bytesReadRate;
    }

    public void setBytesWriteRate(final Long bytesWriteRate) {
        this.bytesWriteRate = bytesWriteRate;
    }

    public void setIopsReadRate(final Long iopsReadRate) {
        this.iopsReadRate = iopsReadRate;
    }

    public void setIopsWriteRate(final Long iopsWriteRate) {
        this.iopsWriteRate = iopsWriteRate;
    }

    public DiskCacheMode getCacheMode() {
        return diskCacheMode;
    }

    public void setCacheMode(final DiskCacheMode cacheMode) {
        diskCacheMode = cacheMode;
    }

    public void setQemuDriver(final boolean qemuDriver) {
        this.qemuDriver = qemuDriver;
    }

    public void setSerial(final String serial) {
        this.serial = serial;
    }

    public enum DeviceType {
        FLOPPY("floppy"), DISK("disk"), CDROM("cdrom"), LUN("lun");
        String type;

        DeviceType(final String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public enum DiskType {
        FILE("file"), BLOCK("block"), DIRECTROY("dir"), NETWORK("network");
        String diskType;

        DiskType(final String type) {
            diskType = type;
        }

        @Override
        public String toString() {
            return diskType;
        }
    }

    public enum DiskProtocol {
        RBD("rbd"), SHEEPDOG("sheepdog"), GLUSTER("gluster");
        String diskProtocol;

        DiskProtocol(final String protocol) {
            diskProtocol = protocol;
        }

        @Override
        public String toString() {
            return diskProtocol;
        }
    }

    public enum DiskBus {
        IDE("ide"), SCSI("scsi"), VIRTIO("virtio"), XEN("xen"), USB("usb"), UML("uml"), FDC("fdc");
        String bus;

        DiskBus(final String bus) {
            this.bus = bus;
        }

        @Override
        public String toString() {
            return bus;
        }
    }

    public enum DiskFmtType {
        RAW("raw"), QCOW2("qcow2");
        String fmtType;

        DiskFmtType(final String fmt) {
            fmtType = fmt;
        }

        @Override
        public String toString() {
            return fmtType;
        }
    }

    public enum DiskCacheMode {
        NONE("none"), WRITEBACK("writeback"), WRITETHROUGH("writethrough");
        String diskCacheMode;

        DiskCacheMode(final String cacheMode) {
            diskCacheMode = cacheMode;
        }

        @Override
        public String toString() {
            if (diskCacheMode == null) {
                return "NONE";
            }
            return diskCacheMode;
        }
    }

    public enum DiscardType {
        IGNORE("ignore"), UNMAP("unmap");
        String _discardType;

        DiscardType(String discardType) {
            _discardType = discardType;
        }

        @Override
        public String toString() {
            if (_discardType == null) {
                return "ignore";
            }
            return _discardType;
        }
    }

    @Override
    public String toString() {
        final StringBuilder diskBuilder = new StringBuilder();
        diskBuilder.append("<disk ");
        if (deviceType != null) {
            diskBuilder.append(" device='" + deviceType + "'");
        }
        diskBuilder.append(" type='" + diskType + "'");
        diskBuilder.append(">\n");
        if (qemuDriver) {
            diskBuilder.append("<driver name='qemu'" + " type='" + diskFmtType
                    + "' cache='" + diskCacheMode + "' ");
            if (discard != null && discard != DiscardType.IGNORE) {
                diskBuilder.append("discard='" + discard.toString() + "' ");
            }
            diskBuilder.append("/>\n");
        }

        if (diskType == DiskType.FILE) {
            diskBuilder.append("<source ");
            if (sourcePath != null) {
                diskBuilder.append("file='" + sourcePath + "'");
            } else if (deviceType == DeviceType.CDROM) {
                diskBuilder.append("file=''");
            }
            diskBuilder.append("/>\n");
        } else if (diskType == DiskType.BLOCK) {
            diskBuilder.append("<source");
            if (sourcePath != null) {
                diskBuilder.append(" dev='" + sourcePath + "'");
            }
            diskBuilder.append("/>\n");
        } else if (diskType == DiskType.NETWORK) {
            diskBuilder.append("<source ");
            diskBuilder.append(" protocol='" + diskProtocol + "'");
            diskBuilder.append(" name='" + sourcePath + "'");
            diskBuilder.append(">\n");
            diskBuilder.append("<host name='");
            diskBuilder.append(sourceHost);
            if (sourcePort != 0) {
                diskBuilder.append("' port='");
                diskBuilder.append(sourcePort);
            }
            diskBuilder.append("'/>\n");
            diskBuilder.append("</source>\n");
            if (authUserName != null) {
                diskBuilder.append("<auth username='" + authUserName + "'>\n");
                diskBuilder.append("<secret type='ceph' uuid='" + authSecretUuid + "'/>\n");
                diskBuilder.append("</auth>\n");
            }
        }
        diskBuilder.append("<target dev='" + diskLabel + "'");
        if (bus != null) {
            diskBuilder.append(" bus='" + bus + "'");
        }
        diskBuilder.append("/>\n");

        if (serial != null && !serial.isEmpty() && deviceType != DeviceType.LUN) {
            diskBuilder.append("<serial>" + serial + "</serial>");
        }

        if (deviceType != DeviceType.CDROM
                && (bytesReadRate != null && bytesReadRate > 0 || bytesWriteRate != null && bytesWriteRate > 0
                || iopsReadRate != null && iopsReadRate > 0 || iopsWriteRate != null && iopsWriteRate > 0)) {
            diskBuilder.append("<iotune>\n");
            if (bytesReadRate != null && bytesReadRate > 0) {
                diskBuilder.append("<read_bytes_sec>" + bytesReadRate + "</read_bytes_sec>\n");
            }
            if (bytesWriteRate != null && bytesWriteRate > 0) {
                diskBuilder.append("<write_bytes_sec>" + bytesWriteRate + "</write_bytes_sec>\n");
            }
            if (iopsReadRate != null && iopsReadRate > 0) {
                diskBuilder.append("<read_iops_sec>" + iopsReadRate + "</read_iops_sec>\n");
            }
            if (iopsWriteRate != null && iopsWriteRate > 0) {
                diskBuilder.append("<write_iops_sec>" + iopsWriteRate + "</write_iops_sec>\n");
            }
            diskBuilder.append("</iotune>\n");
        }

        diskBuilder.append("</disk>\n");
        return diskBuilder.toString();
    }
}
