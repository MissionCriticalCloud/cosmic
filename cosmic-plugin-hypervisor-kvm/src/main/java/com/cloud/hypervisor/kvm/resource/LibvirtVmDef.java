package com.cloud.hypervisor.kvm.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibvirtVmDef {
    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtVmDef.class);
    private static long s_libvirtVersion;
    private static long s_qemuVersion;
    private final Map<String, Object> components = new HashMap<>();
    private String hvsType;
    private String domName;
    private String domUuid;
    private String desc;
    private String platformEmulator;

    public String getHvsType() {
        return hvsType;
    }

    public void setHvsType(final String hvs) {
        hvsType = hvs;
    }

    public void setLibvirtVersion(final long libvirtVersion) {
        setGlobalLibvirtVersion(libvirtVersion);
    }

    public static void setGlobalLibvirtVersion(final long libvirtVersion) {
        s_libvirtVersion = libvirtVersion;
    }

    public void setQemuVersion(final long qemuVersion) {
        setGlobalQemuVersion(qemuVersion);
    }

    public static void setGlobalQemuVersion(final long qemuVersion) {
        s_qemuVersion = qemuVersion;
    }

    public void setDomainName(final String domainName) {
        domName = domainName;
    }

    public void setDomUuid(final String uuid) {
        domUuid = uuid;
    }

    public void setDomDescription(final String desc) {
        this.desc = desc;
    }

    public String getGuestOsType() {
        return desc;
    }

    public String getPlatformEmulator() {
        return platformEmulator;
    }

    public void setPlatformEmulator(final String platformEmulator) {
        this.platformEmulator = platformEmulator;
    }

    public DevicesDef getDevices() {
        final Object o = components.get(DevicesDef.class.toString());
        if (o != null) {
            return (DevicesDef) o;
        }
        return null;
    }

    public MetadataDef getMetaData() {
        MetadataDef metaData = (MetadataDef) components.get(MetadataDef.class.toString());
        if (metaData == null) {
            metaData = new MetadataDef();
            addComp(metaData);
        }
        return metaData;
    }

    public void addComp(final Object comp) {
        components.put(comp.getClass().toString(), comp);
    }

    public static class GuestDef {
        private final List<BootOrder> bootDevs = new ArrayList<>();
        private GuestType type;
        private String arch;
        private String loader;
        private String kernel;
        private String initrd;
        private String root;
        private String cmdline;
        private String uuid;
        private String machine;

        public GuestType getGuestType() {
            return type;
        }

        public void setGuestType(final GuestType type) {
            this.type = type;
        }

        public void setGuestArch(final String arch) {
            this.arch = arch;
        }

        public void setMachineType(final String machine) {
            this.machine = machine;
        }

        public void setLoader(final String loader) {
            this.loader = loader;
        }

        public void setBootKernel(final String kernel, final String initrd, final String rootdev, final String cmdline) {
            this.kernel = kernel;
            this.initrd = initrd;
            root = rootdev;
            this.cmdline = cmdline;
        }

        public void setBootOrder(final BootOrder order) {
            bootDevs.add(order);
        }

        public void setUuid(final String uuid) {
            this.uuid = uuid;
        }

        @Override
        public String toString() {
            if (type == GuestType.KVM) {
                final StringBuilder guestDef = new StringBuilder();

                guestDef.append("<sysinfo type='smbios'>\n");
                guestDef.append("<system>\n");
                guestDef.append("<entry name='manufacturer'>Mission Critical Cloud</entry>\n");
                guestDef.append("<entry name='product'>Cosmic " + type.toString() + " Hypervisor</entry>\n");
                guestDef.append("<entry name='uuid'>" + uuid + "</entry>\n");
                guestDef.append("</system>\n");
                guestDef.append("</sysinfo>\n");

                guestDef.append("<os>\n");
                guestDef.append("<type ");
                if (arch != null) {
                    guestDef.append(" arch='" + arch + "'");
                }
                if (machine != null) {
                    guestDef.append(" machine='" + machine + "'");
                }
                guestDef.append(">hvm</type>\n");
                if (!bootDevs.isEmpty()) {
                    for (final BootOrder bo : bootDevs) {
                        guestDef.append("<boot dev='" + bo + "'/>\n");
                    }
                }
                guestDef.append("<smbios mode='sysinfo'/>\n");
                guestDef.append("</os>\n");
                return guestDef.toString();
            } else {
                return null;
            }
        }

        enum GuestType {
            KVM, XEN, EXE
        }

        enum BootOrder {
            HARDISK("hd"), CDROM("cdrom"), FLOPPY("fd"), NETWORK("network");
            String order;

            BootOrder(final String order) {
                this.order = order;
            }

            @Override
            public String toString() {
                return order;
            }
        }
    }

    public static class GuestResourceDef {
        private long mem;
        private long currentMem = -1;
        private String memBacking;
        private int vcpu = -1;
        private boolean memBalloning = false;

        public void setMemorySize(final long mem) {
            this.mem = mem;
        }

        public void setCurrentMem(final long currMem) {
            currentMem = currMem;
        }

        public void setMemBacking(final String memBacking) {
            this.memBacking = memBacking;
        }

        public void setVcpuNum(final int vcpu) {
            this.vcpu = vcpu;
        }

        public void setMemBalloning(final boolean turnon) {
            memBalloning = turnon;
        }

        @Override
        public String toString() {
            final StringBuilder resBuidler = new StringBuilder();
            resBuidler.append("<memory>" + mem + "</memory>\n");
            if (currentMem != -1) {
                resBuidler.append("<currentMemory>" + currentMem + "</currentMemory>\n");
            }
            if (memBacking != null) {
                resBuidler.append("<memoryBacking>" + "<" + memBacking + "/>" + "</memoryBacking>\n");
            }
            if (memBalloning) {
                resBuidler.append("<devices>\n" + "<memballoon model='virtio'/>\n" + "</devices>\n");
            } else {
                resBuidler.append("<devices>\n" + "<memballoon model='none'/>\n" + "</devices>\n");
            }
            if (vcpu != -1) {
                resBuidler.append("<vcpu>" + vcpu + "</vcpu>\n");
            }
            return resBuidler.toString();
        }
    }

    public static class HyperVEnlightenmentFeatureDef {
        private final Map<String, String> features = new HashMap<>();
        private int retries = 4096; // set to sane default

        public void setFeature(final String feature, final boolean on) {
            if (on && Enlight.isValidFeature(feature)) {
                setFeature(feature);
            }
        }

        private void setFeature(final String feature) {
            features.put(feature, "on");
        }

        enum Enlight {
            RELAX("relaxed"), VAPIC("vapic"), SPIN("spinlocks");

            private final String featureName;

            Enlight(final String featureName) {
                this.featureName = featureName;
            }

            static boolean isValidFeature(final String featureName) {
                final Enlight[] enlights = Enlight.values();
                for (final Enlight e : enlights) {
                    if (e.getFeatureName().equals(featureName)) {
                        return true;
                    }
                }
                return false;
            }

            String getFeatureName() {
                return featureName;
            }
        }

        public void setRetries(final int retry) {
            if (retry >= retries) {
                retries = retry;
            }
        }

        public int getRetries() {
            return retries;
        }

        @Override
        public String toString() {
            final StringBuilder feaBuilder = new StringBuilder();
            feaBuilder.append("<hyperv>\n");
            for (final Map.Entry<String, String> e : features.entrySet()) {
                feaBuilder.append("<");
                feaBuilder.append(e.getKey());

                if (e.getKey().equals("spinlocks")) {
                    feaBuilder.append(" state='" + e.getValue() + "' retries='" + getRetries() + "'");
                } else {
                    feaBuilder.append(" state='" + e.getValue() + "'");
                }

                feaBuilder.append("/>\n");
            }
            feaBuilder.append("</hyperv>\n");
            return feaBuilder.toString();
        }
    }

    public static class FeaturesDef {
        private final List<String> features = new ArrayList<>();

        private HyperVEnlightenmentFeatureDef hyperVEnlightenmentFeatureDef = null;

        public void addFeatures(final String feature) {
            features.add(feature);
        }

        public void addHyperVFeature(final HyperVEnlightenmentFeatureDef hyperVEnlightenmentFeatureDef) {
            this.hyperVEnlightenmentFeatureDef = hyperVEnlightenmentFeatureDef;
        }

        @Override
        public String toString() {
            final StringBuilder feaBuilder = new StringBuilder();
            feaBuilder.append("<features>\n");
            for (final String feature : features) {
                feaBuilder.append("<" + feature + "/>\n");
            }
            if (hyperVEnlightenmentFeatureDef != null) {
                final String hpervF = hyperVEnlightenmentFeatureDef.toString();
                if (!hpervF.isEmpty()) {
                    feaBuilder.append(hpervF);
                }
            }
            feaBuilder.append("</features>\n");
            return feaBuilder.toString();
        }
    }

    public static class TermPolicy {
        private String reboot;
        private String powerOff;
        private String crash;

        public TermPolicy() {
            reboot = powerOff = crash = "destroy";
        }

        public void setRebootPolicy(final String rbPolicy) {
            reboot = rbPolicy;
        }

        public void setPowerOffPolicy(final String poPolicy) {
            powerOff = poPolicy;
        }

        public void setCrashPolicy(final String crashPolicy) {
            crash = crashPolicy;
        }

        @Override
        public String toString() {
            final StringBuilder term = new StringBuilder();
            term.append("<on_reboot>" + reboot + "</on_reboot>\n");
            term.append("<on_poweroff>" + powerOff + "</on_poweroff>\n");
            term.append("<on_crash>" + powerOff + "</on_crash>\n");
            return term.toString();
        }
    }

    public static class ClockDef {
        private ClockOffset offset;
        private String timerName;
        private String tickPolicy;
        private String track;
        private boolean noKvmClock;

        public ClockDef() {
            offset = ClockOffset.UTC;
        }

        public void setClockOffset(final ClockOffset offset) {
            this.offset = offset;
        }

        public void setTimer(final String timerName, final String tickPolicy, final String track, final boolean noKvmClock) {
            this.noKvmClock = noKvmClock;
            setTimer(timerName, tickPolicy, track);
        }

        public void setTimer(final String timerName, final String tickPolicy, final String track) {
            this.timerName = timerName;
            this.tickPolicy = tickPolicy;
            this.track = track;
        }

        public enum ClockOffset {
            UTC("utc"), LOCALTIME("localtime"), TIMEZONE("timezone"), VARIABLE("variable");

            private final String offset;

            private ClockOffset(final String offset) {
                this.offset = offset;
            }

            @Override
            public String toString() {
                return offset;
            }
        }

        @Override
        public String toString() {
            final StringBuilder clockBuilder = new StringBuilder();
            clockBuilder.append("<clock offset='");
            clockBuilder.append(offset.toString());
            clockBuilder.append("'>\n");
            if (timerName != null) {
                clockBuilder.append("<timer name='");
                clockBuilder.append(timerName);
                clockBuilder.append("' ");

                if (timerName.equals("kvmclock") && noKvmClock) {
                    clockBuilder.append("present='no' />");
                } else {
                    if (tickPolicy != null) {
                        clockBuilder.append("tickpolicy='");
                        clockBuilder.append(tickPolicy);
                        clockBuilder.append("' ");
                    }

                    if (track != null) {
                        clockBuilder.append("track='");
                        clockBuilder.append(track);
                        clockBuilder.append("' ");
                    }

                    clockBuilder.append(">\n");
                    clockBuilder.append("</timer>\n");
                }
            }
            clockBuilder.append("</clock>\n");
            return clockBuilder.toString();
        }
    }

    public static class DevicesDef {
        private final Map<String, List<?>> devices = new HashMap<>();
        private String emulator;
        private GuestDef.GuestType guestType;

        public boolean addDevice(final Object device) {
            final Object dev = devices.get(device.getClass().toString());
            if (dev == null) {
                final List<Object> devs = new ArrayList<>();
                devs.add(device);
                devices.put(device.getClass().toString(), devs);
            } else {
                final List<Object> devs = (List<Object>) dev;
                devs.add(device);
            }
            return true;
        }

        public void setEmulatorPath(final String emulator) {
            this.emulator = emulator;
        }

        public void setGuestType(final GuestDef.GuestType guestType) {
            this.guestType = guestType;
        }

        public List<DiskDef> getDisks() {
            return (List<DiskDef>) devices.get(DiskDef.class.toString());
        }

        @Override
        public String toString() {
            final StringBuilder devicesBuilder = new StringBuilder();
            devicesBuilder.append("<devices>\n");
            if (emulator != null) {
                devicesBuilder.append("<emulator>" + emulator + "</emulator>\n");
            }

            for (final List<?> devs : devices.values()) {
                for (final Object dev : devs) {
                    devicesBuilder.append(dev.toString());
                }
            }
            devicesBuilder.append("</devices>\n");
            return devicesBuilder.toString();
        }

        public List<InterfaceDef> getInterfaces() {
            return (List<InterfaceDef>) devices.get(InterfaceDef.class.toString());
        }
    }

    public static class DiskDef {
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
            if (devId == 2) {
                devId++;
            }

            final char suffix = (char) ('a' + devId);
            if (bus == DiskBus.SCSI) {
                return "sd" + suffix;
            } else if (bus == DiskBus.VIRTIO) {
                return "vd" + suffix;
            }
            return "hd" + suffix;
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

        public int getDiskSeq() {
            final char suffix = diskLabel.charAt(diskLabel.length() - 1);
            return suffix - 'a';
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

        enum DiskType {
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
                        + "' cache='" + diskCacheMode + "' " + "/>\n");
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
                    && s_libvirtVersion >= 9008
                    && s_qemuVersion >= 1001000
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

    public static class InterfaceDef {
        private GuestNetType netType; // bridge, ethernet, network, user, internal
        private HostNicType hostNetType; /* Only used by agent java code */
        private String netSourceMode;
        private String sourceName;
        private String networkName;
        private String macAddr;
        private String ipAddr;
        private String scriptPath;
        private NicModel model;
        private Integer networkRateKBps;
        private String virtualPortType;
        private String virtualPortInterfaceId;
        private int vlanTag = -1;

        public void defBridgeNet(final String brName, final String targetBrName, final String macAddr, final NicModel model) {
            defBridgeNet(brName, targetBrName, macAddr, model, 0);
        }

        public void defBridgeNet(final String brName, final String targetBrName, final String macAddr, final NicModel model,
                                 final Integer networkRateKBps) {
            netType = GuestNetType.BRIDGE;
            sourceName = brName;
            networkName = targetBrName;
            this.macAddr = macAddr;
            this.model = model;
            this.networkRateKBps = networkRateKBps;
        }

        public void defDirectNet(final String sourceName, final String targetName, final String macAddr, final NicModel model, final String sourceMode) {
            defDirectNet(sourceName, targetName, macAddr, model, sourceMode, 0);
        }

        public void defDirectNet(final String sourceName, final String targetName, final String macAddr, final NicModel model, final String sourceMode,
                                 final Integer networkRateKBps) {
            netType = GuestNetType.DIRECT;
            netSourceMode = sourceMode;
            this.sourceName = sourceName;
            networkName = targetName;
            this.macAddr = macAddr;
            this.model = model;
            this.networkRateKBps = networkRateKBps;
        }

        public void defPrivateNet(final String networkName, final String targetName, final String macAddr, final NicModel model) {
            defPrivateNet(networkName, targetName, macAddr, model, 0);
        }

        public void defPrivateNet(String networkName, final String targetName, final String macAddr, final NicModel model,
                                  final Integer networkRateKBps) {
            netType = GuestNetType.NETWORK;
            sourceName = networkName;
            networkName = targetName;
            this.macAddr = macAddr;
            this.model = model;
            this.networkRateKBps = networkRateKBps;
        }

        public void defEthernet(final String targetName, final String macAddr, final NicModel model) {
            defEthernet(targetName, macAddr, model, null);
        }

        public void defEthernet(final String targetName, final String macAddr, final NicModel model, final String scriptPath) {
            defEthernet(targetName, macAddr, model, scriptPath, 0);
        }

        public void defEthernet(final String targetName, final String macAddr, final NicModel model, final String scriptPath,
                                final Integer networkRateKBps) {
            netType = GuestNetType.ETHERNET;
            networkName = targetName;
            sourceName = targetName;
            this.macAddr = macAddr;
            this.model = model;
            this.scriptPath = scriptPath;
            this.networkRateKBps = networkRateKBps;
        }

        public HostNicType getHostNetType() {
            return hostNetType;
        }

        public void setHostNetType(final HostNicType hostNetType) {
            this.hostNetType = hostNetType;
        }

        public String getBrName() {
            return sourceName;
        }

        public GuestNetType getNetType() {
            return netType;
        }

        public String getNetSourceMode() {
            return netSourceMode;
        }

        public String getDevName() {
            return networkName;
        }

        public String getMacAddress() {
            return macAddr;
        }

        public NicModel getModel() {
            return model;
        }

        public String getVirtualPortType() {
            return virtualPortType;
        }

        public void setVirtualPortType(final String virtualPortType) {
            this.virtualPortType = virtualPortType;
        }

        public String getVirtualPortInterfaceId() {
            return virtualPortInterfaceId;
        }

        public void setVirtualPortInterfaceId(final String virtualPortInterfaceId) {
            this.virtualPortInterfaceId = virtualPortInterfaceId;
        }

        public int getVlanTag() {
            return vlanTag;
        }

        public void setVlanTag(final int vlanTag) {
            this.vlanTag = vlanTag;
        }

        enum GuestNetType {
            BRIDGE("bridge"), DIRECT("direct"), NETWORK("network"), USER("user"), ETHERNET("ethernet"), INTERNAL("internal");
            String type;

            GuestNetType(final String type) {
                this.type = type;
            }

            @Override
            public String toString() {
                return type;
            }
        }

        enum NicModel {
            E1000("e1000"), VIRTIO("virtio"), RTL8139("rtl8139"), NE2KPCI("ne2k_pci"), VMXNET3("vmxnet3");
            String model;

            NicModel(final String model) {
                this.model = model;
            }

            @Override
            public String toString() {
                return model;
            }
        }

        enum HostNicType {
            DIRECT_ATTACHED_WITHOUT_DHCP, DIRECT_ATTACHED_WITH_DHCP, VNET, VLAN
        }

        @Override
        public String toString() {
            final StringBuilder netBuilder = new StringBuilder();
            netBuilder.append("<interface type='" + netType + "'>\n");
            if (netType == GuestNetType.BRIDGE) {
                netBuilder.append("<source bridge='" + sourceName + "'/>\n");
            } else if (netType == GuestNetType.NETWORK) {
                netBuilder.append("<source network='" + sourceName + "'/>\n");
            } else if (netType == GuestNetType.DIRECT) {
                netBuilder.append("<source dev='" + sourceName + "' mode='" + netSourceMode + "'/>\n");
            }
            if (networkName != null) {
                netBuilder.append("<target dev='" + networkName + "'/>\n");
            }
            if (macAddr != null) {
                netBuilder.append("<mac address='" + macAddr + "'/>\n");
            }
            if (model != null) {
                netBuilder.append("<model type='" + model + "'/>\n");
            }
            if (s_libvirtVersion >= 9004 && networkRateKBps > 0) { // supported from libvirt 0.9.4
                netBuilder.append("<bandwidth>\n");
                netBuilder.append("<inbound average='" + networkRateKBps + "' peak='" + networkRateKBps + "'/>\n");
                netBuilder.append("<outbound average='" + networkRateKBps + "' peak='" + networkRateKBps + "'/>\n");
                netBuilder.append("</bandwidth>\n");
            }
            if (scriptPath != null) {
                netBuilder.append("<script path='" + scriptPath + "'/>\n");
            }
            if (virtualPortType != null) {
                netBuilder.append("<virtualport type='" + virtualPortType + "'>\n");
                if (virtualPortInterfaceId != null) {
                    netBuilder.append("<parameters interfaceid='" + virtualPortInterfaceId + "'/>\n");
                }
                netBuilder.append("</virtualport>\n");
            }
            if (vlanTag > 0 && vlanTag < 4095) {
                netBuilder.append("<vlan trunk='no'>\n<tag id='" + vlanTag + "'/>\n</vlan>");
            }
            netBuilder.append("</interface>\n");
            return netBuilder.toString();
        }
    }

    public static class ConsoleDef {
        private final String ttyPath;
        private final String type;
        private final String source;
        private short port = -1;

        public ConsoleDef(final String type, final String path, final String source, final short port) {
            this.type = type;
            ttyPath = path;
            this.source = source;
            this.port = port;
        }

        @Override
        public String toString() {
            final StringBuilder consoleBuilder = new StringBuilder();
            consoleBuilder.append("<console ");
            consoleBuilder.append("type='" + type + "'");
            if (ttyPath != null) {
                consoleBuilder.append("tty='" + ttyPath + "'");
            }
            consoleBuilder.append(">\n");
            if (source != null) {
                consoleBuilder.append("<source path='" + source + "'/>\n");
            }
            if (port != -1) {
                consoleBuilder.append("<target port='" + port + "'/>\n");
            }
            consoleBuilder.append("</console>\n");
            return consoleBuilder.toString();
        }
    }

    public static class CpuTuneDef {
        private int shares = 0;

        public int getShares() {
            return shares;
        }

        public void setShares(final int shares) {
            this.shares = shares;
        }

        @Override
        public String toString() {
            final StringBuilder cpuTuneBuilder = new StringBuilder();
            cpuTuneBuilder.append("<cputune>\n");
            if (shares > 0) {
                cpuTuneBuilder.append("<shares>" + shares + "</shares>\n");
            }
            cpuTuneBuilder.append("</cputune>\n");
            return cpuTuneBuilder.toString();
        }
    }

    public static class CpuModeDef {
        private String mode;
        private String model;
        private List<String> features;
        private int coresPerSocket = -1;
        private int sockets = -1;

        public void setMode(final String mode) {
            this.mode = mode;
        }

        public void setFeatures(final List<String> features) {
            if (features != null) {
                this.features = features;
            }
        }

        public void setModel(final String model) {
            this.model = model;
        }

        public void setTopology(final int coresPerSocket, final int sockets) {
            this.coresPerSocket = coresPerSocket;
            this.sockets = sockets;
        }

        @Override
        public String toString() {
            final StringBuilder modeBuilder = new StringBuilder();

            // start cpu def, adding mode, model
            if ("custom".equalsIgnoreCase(mode) && model != null) {
                modeBuilder.append("<cpu mode='custom' match='exact'><model fallback='allow'>" + model + "</model>");
            } else if ("host-model".equals(mode)) {
                modeBuilder.append("<cpu mode='host-model'><model fallback='allow'></model>");
            } else if ("host-passthrough".equals(mode)) {
                modeBuilder.append("<cpu mode='host-passthrough'>");
            } else {
                modeBuilder.append("<cpu>");
            }

            if (features != null) {
                for (final String feature : features) {
                    modeBuilder.append("<feature policy='require' name='" + feature + "'/>");
                }
            }

            // add topology
            if (sockets > 0 && coresPerSocket > 0) {
                modeBuilder.append("<topology sockets='" + sockets + "' cores='" + coresPerSocket + "' threads='1' />");
            }

            // close cpu def
            modeBuilder.append("</cpu>");
            return modeBuilder.toString();
        }
    }

    public static class SerialDef {
        private final String type;
        private final String source;
        private short port = -1;

        public SerialDef(final String type, final String source, final short port) {
            this.type = type;
            this.source = source;
            this.port = port;
        }

        @Override
        public String toString() {
            final StringBuilder serialBuidler = new StringBuilder();
            serialBuidler.append("<serial type='" + type + "'>\n");
            if (source != null) {
                serialBuidler.append("<source path='" + source + "'/>\n");
            }
            if (port != -1) {
                serialBuidler.append("<target port='" + port + "'/>\n");
            }
            serialBuidler.append("</serial>\n");
            return serialBuidler.toString();
        }
    }

    public static class VideoDef {
        private final String videoModel;
        private final int videoRam;

        public VideoDef(final String videoModel, final int videoRam) {
            this.videoModel = videoModel;
            this.videoRam = videoRam;
        }

        @Override
        public String toString() {
            final StringBuilder videoBuilder = new StringBuilder();
            if (videoModel != null && !videoModel.isEmpty() && videoRam != 0) {
                videoBuilder.append("<video>\n");
                videoBuilder.append("<model type='" + videoModel + "' vram='" + videoRam + "'/>\n");
                videoBuilder.append("</video>\n");
                return videoBuilder.toString();
            }
            return "";
        }
    }

    public static class VirtioSerialDef {
        private final String name;
        private String path;

        public VirtioSerialDef(final String name, final String path) {
            this.name = name;
            this.path = path;
        }

        @Override
        public String toString() {
            final StringBuilder virtioSerialBuilder = new StringBuilder();
            if (path == null) {
                path = "/var/lib/libvirt/qemu";
            }
            // Used by patchviasocket.pl
            virtioSerialBuilder.append("<channel type='unix'>\n");
            virtioSerialBuilder.append("<source mode='bind' path='" + path + "/" + name + ".agent'/>\n");
            virtioSerialBuilder.append("<target type='virtio' name='" + name + ".vport'/>\n");
            virtioSerialBuilder.append("<address type='virtio-serial'/>\n");
            virtioSerialBuilder.append("</channel>\n");
            return virtioSerialBuilder.toString();
        }
    }

    public static class QemuGuestAgentDef {
        @Override
        public String toString() {
            final StringBuilder qemuGuestAgentBuilder = new StringBuilder();

            qemuGuestAgentBuilder.append("<channel type='unix'>\n");
            qemuGuestAgentBuilder.append("<source mode='bind'/>\n");
            qemuGuestAgentBuilder.append("<target type='virtio' name='org.qemu.guest_agent.0'/>\n");
            qemuGuestAgentBuilder.append("</channel>\n");
            return qemuGuestAgentBuilder.toString();
        }
    }

    public static class GraphicDef {
        private final String type;
        private final String listenAddr;
        private final String passwd;
        private final String keyMap;
        private short port = -2;
        private boolean autoPort = false;

        public GraphicDef(final String type, final short port, final boolean autoPort, final String listenAddr, final String passwd, final String keyMap) {
            this.type = type;
            this.port = port;
            this.autoPort = autoPort;
            this.listenAddr = listenAddr;
            this.passwd = StringEscapeUtils.escapeXml(passwd);
            this.keyMap = keyMap;
        }

        @Override
        public String toString() {
            final StringBuilder graphicBuilder = new StringBuilder();
            graphicBuilder.append("<graphics type='" + type + "'");
            if (autoPort) {
                graphicBuilder.append(" autoport='yes'");
            } else if (port != -2) {
                graphicBuilder.append(" port='" + port + "'");
            }
            if (listenAddr != null) {
                graphicBuilder.append(" listen='" + listenAddr + "'");
            } else {
                graphicBuilder.append(" listen=''");
            }
            if (passwd != null) {
                graphicBuilder.append(" passwd='" + passwd + "'");
            } else if (keyMap != null) {
                graphicBuilder.append(" _keymap='" + keyMap + "'");
            }
            graphicBuilder.append("/>\n");
            return graphicBuilder.toString();
        }
    }

    public static class InputDef {
        private final String type; /* tablet, mouse */
        private final String bus; /* ps2, usb, xen */

        public InputDef(final String type, final String bus) {
            this.type = type;
            this.bus = bus;
        }

        @Override
        public String toString() {
            final StringBuilder inputBuilder = new StringBuilder();
            inputBuilder.append("<input type='" + type + "'");
            if (bus != null) {
                inputBuilder.append(" bus='" + bus + "'");
            }
            inputBuilder.append("/>\n");
            return inputBuilder.toString();
        }
    }

    public static class FilesystemDef {
        private final String sourcePath;
        private final String targetPath;

        public FilesystemDef(final String sourcePath, final String targetPath) {
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
        }

        @Override
        public String toString() {
            final StringBuilder fsBuilder = new StringBuilder();
            fsBuilder.append("<filesystem type='mount'>\n");
            fsBuilder.append("  <source dir='" + sourcePath + "'/>\n");
            fsBuilder.append("  <target dir='" + targetPath + "'/>\n");
            fsBuilder.append("</filesystem>\n");
            return fsBuilder.toString();
        }
    }

    public static class MetadataDef {
        Map<String, Object> customNodes = new HashMap<>();

        public <T> T getMetadataNode(final Class<T> fieldClass) {
            T field = (T) customNodes.get(fieldClass.getName());
            if (field == null) {
                try {
                    field = fieldClass.newInstance();
                    customNodes.put(field.getClass().getName(), field);
                } catch (InstantiationException | IllegalAccessException e) {
                    s_logger.debug("No default constructor available in class " + fieldClass.getName() + ", ignoring exception",
                            e);
                }
            }
            return field;
        }

        @Override
        public String toString() {
            final StringBuilder fsBuilder = new StringBuilder();
            fsBuilder.append("<metadata>\n");
            for (final Object field : customNodes.values()) {
                fsBuilder.append(field.toString());
            }
            fsBuilder.append("</metadata>\n");
            return fsBuilder.toString();
        }
    }

    public static class NuageExtensionDef {
        private final Map<String, String> addresses = Maps.newHashMap();

        public void addNuageExtension(final String macAddress, final String vrIp) {
            addresses.put(macAddress, vrIp);
        }

        @Override
        public String toString() {
            final StringBuilder fsBuilder = new StringBuilder();
            for (final Map.Entry<String, String> address : addresses.entrySet()) {
                fsBuilder.append("<nuage-extension>\n").append("  <interface mac='").append(address.getKey()).append(
                        "' vsp-vr-ip='").append(address.getValue()).append("'></interface>\n").append("</nuage-extension>\n");
            }
            return fsBuilder.toString();
        }
    }

    public static class RngDef {
        private String path = "/dev/random";
        private RngModel rngModel = RngModel.VIRTIO;
        private RngBackendModel rngBackendModel = RngBackendModel.RANDOM;

        public RngDef(final String path) {
            this.path = path;
        }

        public RngDef(final RngModel rngModel) {
            this.rngModel = rngModel;
        }

        public RngDef(final RngBackendModel rngBackendModel) {
            this.rngBackendModel = rngBackendModel;
        }

        public RngDef(final String path, final RngBackendModel rngBackendModel) {
            this.path = path;
            this.rngBackendModel = rngBackendModel;
        }

        public RngDef(final String path, final RngModel rngModel) {
            this.path = path;
            this.rngModel = rngModel;
        }

        public String getPath() {
            return path;
        }

        public RngBackendModel getRngBackendModel() {
            return rngBackendModel;
        }

        public RngModel getRngModel() {
            return rngModel;
        }

        enum RngModel {
            VIRTIO("virtio");
            String model;

            RngModel(final String model) {
                this.model = model;
            }

            @Override
            public String toString() {
                return model;
            }
        }

        enum RngBackendModel {
            RANDOM("random"), EGD("egd");
            String model;

            RngBackendModel(final String model) {
                this.model = model;
            }

            @Override
            public String toString() {
                return model;
            }
        }

        @Override
        public String toString() {
            final StringBuilder rngBuilder = new StringBuilder();
            rngBuilder.append("<rng model='" + rngModel + "'>\n");
            rngBuilder.append("<backend model='" + rngBackendModel + "'>" + path + "</backend>");
            rngBuilder.append("</rng>\n");
            return rngBuilder.toString();
        }
    }

    public static class WatchDogDef {
        WatchDogModel model = WatchDogModel.I6300ESB;
        WatchDogAction action = WatchDogAction.NONE;

        public WatchDogDef(final WatchDogAction action) {
            this.action = action;
        }

        public WatchDogDef(final WatchDogModel model) {
            this.model = model;
        }

        public WatchDogDef(final WatchDogAction action, final WatchDogModel model) {
            this.action = action;
            this.model = model;
        }

        public WatchDogAction getAction() {
            return action;
        }

        public WatchDogModel getModel() {
            return model;
        }

        enum WatchDogModel {
            I6300ESB("i6300esb"), IB700("ib700"), DIAG288("diag288");
            String model;

            WatchDogModel(final String model) {
                this.model = model;
            }

            @Override
            public String toString() {
                return model;
            }
        }

        enum WatchDogAction {
            RESET("reset"), SHUTDOWN("shutdown"), POWEROFF("poweroff"), PAUSE("pause"), NONE("none"), DUMP("dump");
            String action;

            WatchDogAction(final String action) {
                this.action = action;
            }

            @Override
            public String toString() {
                return action;
            }
        }

        @Override
        public String toString() {
            final StringBuilder wacthDogBuilder = new StringBuilder();
            wacthDogBuilder.append("<watchdog model='" + model + "' action='" + action + "'/>\n");
            return wacthDogBuilder.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder vmBuilder = new StringBuilder();
        vmBuilder.append("<domain type='" + hvsType + "'>\n");
        vmBuilder.append("<name>" + domName + "</name>\n");
        if (domUuid != null) {
            vmBuilder.append("<uuid>" + domUuid + "</uuid>\n");
        }
        if (desc != null) {
            vmBuilder.append("<description>" + desc + "</description>\n");
        }
        for (final Object o : components.values()) {
            vmBuilder.append(o.toString());
        }
        vmBuilder.append("</domain>\n");
        return vmBuilder.toString();
    }
}
