package com.cloud.hypervisor.kvm.resource;

import org.apache.cloudstack.utils.qemu.QemuImg;

import org.apache.commons.lang.NotImplementedException;

public class LibvirtStorageVolumeDef {
    private final String volName;
    private final Long volSize;
    private final VolumeFormat volFormat;
    private final String backingPath;
    private final VolumeFormat backingFormat;

    public LibvirtStorageVolumeDef(final String volName, final Long size, final VolumeFormat format, final String tmplPath,
                                   final VolumeFormat tmplFormat) {
        this.volName = volName;
        volSize = size;
        volFormat = format;
        backingPath = tmplPath;
        backingFormat = tmplFormat;
    }

    public VolumeFormat getFormat() {
        return volFormat;
    }

    @Override
    public String toString() {
        final StringBuilder storageVolBuilder = new StringBuilder();
        storageVolBuilder.append("<volume>\n");
        storageVolBuilder.append("<name>" + volName + "</name>\n");
        if (volSize != null) {
            storageVolBuilder.append("<capacity>" + volSize + "</capacity>\n");
        }
        storageVolBuilder.append("<target>\n");
        storageVolBuilder.append("<format type='" + volFormat + "'/>\n");
        storageVolBuilder.append("<permissions>");
        storageVolBuilder.append("<mode>0744</mode>");
        storageVolBuilder.append("</permissions>");
        storageVolBuilder.append("</target>\n");
        if (backingPath != null) {
            storageVolBuilder.append("<backingStore>\n");
            storageVolBuilder.append("<path>" + backingPath + "</path>\n");
            storageVolBuilder.append("<format type='" + backingFormat + "'/>\n");
            storageVolBuilder.append("</backingStore>\n");
        }
        storageVolBuilder.append("</volume>\n");
        return storageVolBuilder.toString();
    }

    public enum VolumeFormat {
        RAW("raw"), QCOW2("qcow2"), DIR("dir"), TAR("tar");
        private final String format;

        VolumeFormat(final String format) {
            this.format = format;
        }

        public static VolumeFormat getFormat(final String format) {
            if (format == null) {
                return null;
            }
            if (format.equalsIgnoreCase("raw")) {
                return RAW;
            } else if (format.equalsIgnoreCase("qcow2")) {
                return QCOW2;
            } else if (format.equalsIgnoreCase("dir")) {
                return DIR;
            } else if (format.equalsIgnoreCase("tar")) {
                return TAR;
            }
            return null;
        }

        public static VolumeFormat getFormat(final QemuImg.PhysicalDiskFormat format) {
            switch (format) {
                case RAW:
                    return RAW;
                case QCOW2:
                    return QCOW2;
                case DIR:
                    return DIR;
                case TAR:
                    return TAR;
                default:
                    throw new NotImplementedException();
            }
        }

        @Override
        public String toString() {
            return format;
        }
    }
}
