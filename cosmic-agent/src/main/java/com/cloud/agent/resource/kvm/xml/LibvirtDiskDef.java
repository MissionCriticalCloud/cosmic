package com.cloud.agent.resource.kvm.xml;

import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.ImageFormat;

public class LibvirtDiskDef {
    private DeviceType deviceType;
    private DiskType diskType;
    private DiskProtocol diskProtocol;
    private String sourcePath;
    private String sourceHost;
    private int sourcePort;
    private String authUserName;
    private String authSecretUuid;
    private String diskLabel;
    private DiskControllerType bus;
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
    private Integer deviceId;
    private ImageFormat imageFormat;

    public DiscardType getDiscard() {
        return this.discard;
    }

    public void setDiscard(final DiscardType discard) {
        this.discard = discard;
    }

    public void defFileBasedDisk(final String filePath, final String diskLabel, final DiskControllerType bus, final ImageFormat imageFormat) {
        this.diskType = DiskType.FILE;
        this.deviceType = DeviceType.DISK;
        this.diskCacheMode = DiskCacheMode.NONE;
        this.sourcePath = filePath;
        this.diskLabel = diskLabel;
        this.imageFormat = imageFormat;
        this.bus = bus;
    }

    public void defFileBasedDisk(final String filePath, final int devId, final DiskControllerType bus, final ImageFormat imageFormat) {
        this.diskType = DiskType.FILE;
        this.deviceType = DeviceType.DISK;
        this.diskCacheMode = DiskCacheMode.NONE;
        this.sourcePath = filePath;
        this.diskLabel = getDevLabel(devId, bus);
        this.imageFormat = imageFormat;
        this.bus = bus;
    }

    /* skip iso label */
    private String getDevLabel(int devId, final DiskControllerType bus) {
        if (devId < 0) {
            return "";
        }

        if (devId == 2) {
            devId++;
        }

        if (bus == DiskControllerType.SCSI) {
            return "sd" + getDevLabelSuffix(devId);
        } else if (bus == DiskControllerType.VIRTIO) {
            return "vd" + getDevLabelSuffix(devId);
        }
        return "hd" + getDevLabelSuffix(devId);
    }

    private String getDevLabelSuffix(int deviceIndex) {
        if (deviceIndex < 0) {
            return "";
        }

        final int base = 'z' - 'a' + 1;
        String labelSuffix = "";
        do {
            final char suffix = (char) ('a' + (deviceIndex % base));
            labelSuffix = suffix + labelSuffix;
            deviceIndex = (deviceIndex / base) - 1;
        } while (deviceIndex >= 0);

        return labelSuffix;
    }

    public void defIsoDisk(final String volPath) {
        this.diskType = DiskType.FILE;
        this.deviceType = DeviceType.CDROM;
        this.sourcePath = volPath;
        this.diskLabel = "hdc";
        this.imageFormat = ImageFormat.RAW;
        this.diskCacheMode = DiskCacheMode.NONE;
        this.bus = DiskControllerType.IDE;
    }

    public void defBlockBasedDisk(final String diskName, final String diskLabel, final DiskControllerType bus) {
        this.diskType = DiskType.BLOCK;
        this.deviceType = DeviceType.DISK;
        this.imageFormat = ImageFormat.RAW;
        this.diskCacheMode = DiskCacheMode.NONE;
        this.sourcePath = diskName;
        this.diskLabel = diskLabel;
        this.bus = bus;
    }

    public void defBlockBasedDisk(final String diskName, final int devId, final DiskControllerType bus) {
        this.diskType = DiskType.BLOCK;
        this.deviceType = DeviceType.DISK;
        this.imageFormat = ImageFormat.RAW;
        this.diskCacheMode = DiskCacheMode.NONE;
        this.sourcePath = diskName;
        this.diskLabel = getDevLabel(devId, bus);
        this.bus = bus;
    }

    public void defNetworkBasedDisk(final String diskName, final String sourceHost, final int sourcePort, final String authUserName,
                                    final String authSecretUuid, final int devId, final DiskControllerType bus,
                                    final DiskProtocol protocol, final ImageFormat imageFormat) {
        this.diskType = DiskType.NETWORK;
        this.deviceType = DeviceType.DISK;
        this.imageFormat = imageFormat;
        this.diskCacheMode = DiskCacheMode.NONE;
        this.sourcePath = diskName;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        this.authUserName = authUserName;
        this.authSecretUuid = authSecretUuid;
        this.diskLabel = getDevLabel(devId, bus);
        this.bus = bus;
        this.diskProtocol = protocol;
    }

    public void defNetworkBasedDisk(final String diskName, final String sourceHost, final int sourcePort, final String authUserName,
                                    final String authSecretUuid, final String diskLabel, final DiskControllerType bus,
                                    final DiskProtocol protocol, final ImageFormat imageFormat) {
        this.diskType = DiskType.NETWORK;
        this.deviceType = DeviceType.DISK;
        this.imageFormat = imageFormat;
        this.diskCacheMode = DiskCacheMode.NONE;
        this.sourcePath = diskName;
        this.sourceHost = sourceHost;
        this.sourcePort = sourcePort;
        this.authUserName = authUserName;
        this.authSecretUuid = authSecretUuid;
        this.diskLabel = diskLabel;
        this.bus = bus;
        this.diskProtocol = protocol;
    }

    public void setReadonly() {
        this.readonly = true;
    }

    public void setSharable() {
        this.shareable = true;
    }

    public boolean isAttachDeferred() {
        return this.deferAttach;
    }

    public void setAttachDeferred(final boolean deferAttach) {
        this.deferAttach = deferAttach;
    }

    public String getDiskPath() {
        return this.sourcePath;
    }

    public void setDiskPath(final String volPath) {
        this.sourcePath = volPath;
    }

    public String getDiskLabel() {
        return this.diskLabel;
    }

    public DiskType getDiskType() {
        return this.diskType;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(final DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public DiskControllerType getBusType() {
        return this.bus;
    }

    public ImageFormat getDiskFormatType() {
        return this.imageFormat;
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
        return this.diskCacheMode;
    }

    public void setCacheMode(final DiskCacheMode cacheMode) {
        this.diskCacheMode = cacheMode;
    }

    public void setQemuDriver(final boolean qemuDriver) {
        this.qemuDriver = qemuDriver;
    }

    public void setSerial(final String serial) {
        this.serial = serial;
    }

    public Integer getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(final Integer deviceId) {
        this.deviceId = deviceId;
    }

    public void setImageFormat(final ImageFormat imageFormat) {
        this.imageFormat = imageFormat;
    }

    public enum DeviceType {
        FLOPPY("floppy"), DISK("disk"), CDROM("cdrom"), LUN("lun");
        String type;

        DeviceType(final String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public enum DiskType {
        FILE("file"), BLOCK("block"), DIRECTROY("dir"), NETWORK("network");
        String diskType;

        DiskType(final String type) {
            this.diskType = type;
        }

        @Override
        public String toString() {
            return this.diskType;
        }
    }

    public enum DiskProtocol {
        RBD("rbd"), SHEEPDOG("sheepdog"), GLUSTER("gluster");
        String diskProtocol;

        DiskProtocol(final String protocol) {
            this.diskProtocol = protocol;
        }

        @Override
        public String toString() {
            return this.diskProtocol;
        }
    }

    public enum DiskBus {
        XEN("xen"), USB("usb"), UML("uml"), FDC("fdc");
        String bus;

        DiskBus(final String bus) {
            this.bus = bus;
        }

        @Override
        public String toString() {
            return this.bus;
        }
    }

    public enum DiskCacheMode {
        NONE("none"), WRITEBACK("writeback"), WRITETHROUGH("writethrough");
        String diskCacheMode;

        DiskCacheMode(final String cacheMode) {
            this.diskCacheMode = cacheMode;
        }

        @Override
        public String toString() {
            if (this.diskCacheMode == null) {
                return "NONE";
            }
            return this.diskCacheMode;
        }
    }

    public enum DiscardType {
        IGNORE("ignore"), UNMAP("unmap");
        String _discardType;

        DiscardType(final String discardType) {
            this._discardType = discardType;
        }

        @Override
        public String toString() {
            if (this._discardType == null) {
                return "ignore";
            }
            return this._discardType;
        }
    }

    @Override
    public String toString() {
        final StringBuilder diskBuilder = new StringBuilder();
        diskBuilder.append("<disk ");
        if (this.deviceType != null) {
            diskBuilder.append(" device='" + this.deviceType + "'");
        }
        diskBuilder.append(" type='" + this.diskType + "'");
        diskBuilder.append(">\n");
        if (this.qemuDriver) {
            diskBuilder.append("<driver name='qemu'" + " type='" + this.imageFormat.toString().toLowerCase()
                    + "' cache='" + this.diskCacheMode + "' ");
            if (this.discard != null && this.discard != DiscardType.IGNORE) {
                diskBuilder.append("discard='" + this.discard.toString() + "' ");
            }
            diskBuilder.append("/>\n");
        }

        if (this.diskType == DiskType.FILE) {
            diskBuilder.append("<source ");
            if (this.sourcePath != null) {
                diskBuilder.append("file='" + this.sourcePath + "'");
            } else if (this.deviceType == DeviceType.CDROM) {
                diskBuilder.append("file=''");
            }
            diskBuilder.append("/>\n");
        } else if (this.diskType == DiskType.BLOCK) {
            diskBuilder.append("<source");
            if (this.sourcePath != null) {
                diskBuilder.append(" dev='" + this.sourcePath + "'");
            }
            diskBuilder.append("/>\n");
        } else if (this.diskType == DiskType.NETWORK) {
            diskBuilder.append("<source ");
            diskBuilder.append(" protocol='" + this.diskProtocol + "'");
            diskBuilder.append(" name='" + this.sourcePath + "'");
            diskBuilder.append(">\n");
            diskBuilder.append("<host name='");
            diskBuilder.append(this.sourceHost);
            if (this.sourcePort != 0) {
                diskBuilder.append("' port='");
                diskBuilder.append(this.sourcePort);
            }
            diskBuilder.append("'/>\n");
            diskBuilder.append("</source>\n");
            if (this.authUserName != null) {
                diskBuilder.append("<auth username='" + this.authUserName + "'>\n");
                diskBuilder.append("<secret type='ceph' uuid='" + this.authSecretUuid + "'/>\n");
                diskBuilder.append("</auth>\n");
            }
        }
        diskBuilder.append("<target dev='" + this.diskLabel + "'");
        if (this.bus != null) {
            diskBuilder.append(" bus='" + this.bus.toString().toLowerCase() + "'");
        }
        diskBuilder.append("/>\n");

        if (this.serial != null && !this.serial.isEmpty() && this.deviceType != DeviceType.LUN) {
            diskBuilder.append("<serial>" + this.serial + "</serial>");
        }

        if (this.deviceType != DeviceType.CDROM
                && (this.bytesReadRate != null && this.bytesReadRate > 0 || this.bytesWriteRate != null && this.bytesWriteRate > 0
                || this.iopsReadRate != null && this.iopsReadRate > 0 || this.iopsWriteRate != null && this.iopsWriteRate > 0)) {
            diskBuilder.append("<iotune>\n");
            if (this.bytesReadRate != null && this.bytesReadRate > 0) {
                diskBuilder.append("<read_bytes_sec>" + this.bytesReadRate + "</read_bytes_sec>\n");
            }
            if (this.bytesWriteRate != null && this.bytesWriteRate > 0) {
                diskBuilder.append("<write_bytes_sec>" + this.bytesWriteRate + "</write_bytes_sec>\n");
            }
            if (this.iopsReadRate != null && this.iopsReadRate > 0) {
                diskBuilder.append("<read_iops_sec>" + this.iopsReadRate + "</read_iops_sec>\n");
            }
            if (this.iopsWriteRate != null && this.iopsWriteRate > 0) {
                diskBuilder.append("<write_iops_sec>" + this.iopsWriteRate + "</write_iops_sec>\n");
            }
            diskBuilder.append("</iotune>\n");
        }

        if (this.bus == DiskControllerType.SCSI && getDeviceId() != null) {
            diskBuilder.append("<address type='drive' controller='0' bus='0' target='0' unit='" + getDeviceId().toString() + "'/>");
        }
        diskBuilder.append("</disk>\n");
        return diskBuilder.toString();
    }
}
