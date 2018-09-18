package com.cloud.agent.resource.kvm.xml;

import com.cloud.model.enumeration.GuestNetType;
import com.cloud.model.enumeration.NicModel;
import com.cloud.model.enumeration.RngBackendModel;
import com.cloud.model.enumeration.RngModel;
import com.cloud.model.enumeration.WatchDogAction;
import com.cloud.model.enumeration.WatchDogModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

public class LibvirtVmDef {
    private static long s_libvirtVersion;
    private static long s_qemuVersion;
    private final Map<String, Object> components = new HashMap<>();
    private String hvsType;
    private String domName;
    private String domUuid;
    private String desc;
    private String platformEmulator;

    public String getHvsType() {
        return this.hvsType;
    }

    public void setHvsType(final String hvs) {
        this.hvsType = hvs;
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
        this.domName = domainName;
    }

    public void setDomUuid(final String uuid) {
        this.domUuid = uuid;
    }

    public void setDomDescription(final String desc) {
        this.desc = desc;
    }

    public String getGuestOsType() {
        return this.desc;
    }

    public String getPlatformEmulator() {
        return this.platformEmulator;
    }

    public void setPlatformEmulator(final String platformEmulator) {
        this.platformEmulator = platformEmulator;
    }

    public DevicesDef getDevices() {
        final Object o = this.components.get(DevicesDef.class.toString());
        if (o != null) {
            return (DevicesDef) o;
        }
        return null;
    }

    public void addComponent(final Object component) {
        this.components.put(component.getClass().toString(), component);
    }

    public enum GuestType {
        KVM, XEN, EXE
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
        private String manufacturer;
        private Long bootMenuTimeout = 0L;

        public GuestType getGuestType() {
            return this.type;
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
            this.root = rootdev;
            this.cmdline = cmdline;
        }

        public void setBootOrder(final BootOrder order) {
            this.bootDevs.add(order);
        }

        public void setUuid(final String uuid) {
            this.uuid = uuid;
        }

        public void setManufacturer(final String manufacturer) {
            this.manufacturer = manufacturer;
        }

        public void setBootMenuTimeout(final Long bootMenuTimeout) {
            this.bootMenuTimeout = bootMenuTimeout;
        }

        @Override
        public String toString() {
            if (this.type == GuestType.KVM) {
                final StringBuilder guestDef = new StringBuilder();

                guestDef.append("<sysinfo type='smbios'>\n");
                guestDef.append("<system>\n");
                if (this.manufacturer != null && !this.manufacturer.isEmpty()) {
                    guestDef.append("<entry name='manufacturer'>" + this.manufacturer + "</entry>\n");
                }
                guestDef.append("<entry name='product'>Cosmic " + this.type.toString() + " Hypervisor</entry>\n");
                guestDef.append("<entry name='uuid'>" + this.uuid + "</entry>\n");
                guestDef.append("</system>\n");
                guestDef.append("</sysinfo>\n");

                guestDef.append("<os>\n");
                guestDef.append("<type ");
                if (this.arch != null) {
                    guestDef.append(" arch='" + this.arch + "'");
                }
                if (this.machine != null) {
                    guestDef.append(" machine='" + this.machine + "'");
                }
                guestDef.append(">hvm</type>\n");
                if (!this.bootDevs.isEmpty()) {
                    for (final BootOrder bo : this.bootDevs) {
                        guestDef.append("<boot dev='" + bo + "'/>\n");
                    }
                }
                guestDef.append("<smbios mode='sysinfo'/>\n");

                if (this.bootMenuTimeout != null) {
                    if (this.bootMenuTimeout > 0) {
                        guestDef.append("<bootmenu enable='yes' timeout='" + this.bootMenuTimeout + "'/>\n");
                    }
                }

                guestDef.append("</os>\n");
                return guestDef.toString();
            } else {
                return null;
            }
        }

        public enum BootOrder {
            HARDISK("hd"), CDROM("cdrom"), FLOPPY("fd"), NETWORK("network");
            String order;

            BootOrder(final String order) {
                this.order = order;
            }

            @Override
            public String toString() {
                return this.order;
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
            this.currentMem = currMem;
        }

        public void setMemBacking(final String memBacking) {
            this.memBacking = memBacking;
        }

        public void setVcpuNum(final int vcpu) {
            this.vcpu = vcpu;
        }

        public void setMemBalloning(final boolean turnon) {
            this.memBalloning = turnon;
        }

        @Override
        public String toString() {
            final StringBuilder resBuidler = new StringBuilder();
            resBuidler.append("<memory>" + this.mem + "</memory>\n");
            if (this.currentMem != -1) {
                resBuidler.append("<currentMemory>" + this.currentMem + "</currentMemory>\n");
            }
            if (this.memBacking != null) {
                resBuidler.append("<memoryBacking>" + "<" + this.memBacking + "/>" + "</memoryBacking>\n");
            }
            if (this.memBalloning) {
                resBuidler.append("<devices>\n" + "<memballoon model='virtio'/>\n" + "</devices>\n");
            } else {
                resBuidler.append("<devices>\n" + "<memballoon model='none'/>\n" + "</devices>\n");
            }
            if (this.vcpu != -1) {
                resBuidler.append("<vcpu>" + this.vcpu + "</vcpu>\n");
            }
            return resBuidler.toString();
        }
    }

    public static class HyperVEnlightenmentFeatureDef {
        private final Map<String, String> features = new HashMap<>();
        private int retries = 4096; // set to sane default

        public void addFeature(final String feature, final boolean on) {
            if (on && Enlight.isValidFeature(feature)) {
                addFeature(feature);
            }
        }

        private void addFeature(final String feature) {
            this.features.put(feature, "on");
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
                return this.featureName;
            }
        }

        public void setRetries(final int retry) {
            if (retry > this.retries) {
                this.retries = retry;
            }
        }

        public int getRetries() {
            return this.retries;
        }

        @Override
        public String toString() {
            final StringBuilder feaBuilder = new StringBuilder();
            feaBuilder.append("<hyperv>\n");
            for (final Map.Entry<String, String> e : this.features.entrySet()) {
                feaBuilder.append("<");
                feaBuilder.append(e.getKey());

                if (e.getKey().equals("spinlocks")) {
                    feaBuilder.append(" state='").append(e.getValue()).append("' retries='").append(getRetries()).append("'");
                } else {
                    feaBuilder.append(" state='").append(e.getValue()).append("'");
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

        public void addFeature(final String feature) {
            this.features.add(feature);
        }

        public void addHyperVFeature(final HyperVEnlightenmentFeatureDef hyperVEnlightenmentFeatureDef) {
            this.hyperVEnlightenmentFeatureDef = hyperVEnlightenmentFeatureDef;
        }

        @Override
        public String toString() {
            final StringBuilder feaBuilder = new StringBuilder();
            feaBuilder.append("<features>\n");
            for (final String feature : this.features) {
                feaBuilder.append("<" + feature + "/>\n");
            }
            if (this.hyperVEnlightenmentFeatureDef != null) {
                final String hpervF = this.hyperVEnlightenmentFeatureDef.toString();
                if (!hpervF.isEmpty()) {
                    feaBuilder.append(hpervF);
                }
            }
            feaBuilder.append("</features>\n");
            return feaBuilder.toString();
        }
    }

    public static class TermPolicy {
        private static final String DEFAULT_VALUE = "destroy";
        private String reboot = DEFAULT_VALUE;
        private String powerOff = DEFAULT_VALUE;
        private String crash = DEFAULT_VALUE;

        public void setRebootPolicy(final String rbPolicy) {
            this.reboot = rbPolicy;
        }

        public void setPowerOffPolicy(final String poPolicy) {
            this.powerOff = poPolicy;
        }

        public void setCrashPolicy(final String crashPolicy) {
            this.crash = crashPolicy;
        }

        @Override
        public String toString() {
            final StringBuilder term = new StringBuilder();
            term.append("<on_reboot>" + this.reboot + "</on_reboot>\n");
            term.append("<on_poweroff>" + this.powerOff + "</on_poweroff>\n");
            term.append("<on_crash>" + this.crash + "</on_crash>\n");
            return term.toString();
        }
    }

    public static class ClockDef {
        private ClockOffset offset;
        private final List<Timer> timers = new ArrayList<>();

        public static class Timer {
            private String name;
            private String tickPolicy;
            private String track;
            private boolean noKvmClock;
            private boolean present;
            private boolean trackGuest;

            public Timer() {
            }

            private Timer(final String name, final String tickPolicy, final boolean present, final boolean trackGuest) {
                this.name = name;
                this.tickPolicy = tickPolicy;
                this.track = track;
                this.noKvmClock = noKvmClock;
                this.present = present;
                this.trackGuest = trackGuest;
            }

            @Override
            public String toString() {
                final StringBuilder timerBuilder = new StringBuilder();
                if (this.name != null) {
                    timerBuilder.append("<timer name='");
                    timerBuilder.append(this.name);
                    timerBuilder.append("' ");

                    if ("hpet".equals(name) || "kvmclock".equals(name) || "hypervclock".equals(name)) {
                        timerBuilder.append("present='").append(present ? "yes" : "no").append("' />");
                    } else {
                        if (this.tickPolicy != null) {
                            timerBuilder.append("tickpolicy='");
                            timerBuilder.append(this.tickPolicy);
                            timerBuilder.append("' ");
                            if (this.trackGuest) {
                                timerBuilder.append("track='guest'");
                                timerBuilder.append(" ");
                            }
                        }

                        timerBuilder.append(">\n");
                        timerBuilder.append("</timer>\n");
                    }
                }

                return timerBuilder.toString();
            }
        }

        public ClockDef() {
            this.offset = ClockOffset.UTC;
        }

        public void setClockOffset(final ClockOffset offset) {
            this.offset = offset;
        }

        public void addTimer(final String timerName, final String tickPolicy, final boolean present, final boolean trackGuest) {
            timers.add(new Timer(timerName, tickPolicy, present, trackGuest));
        }

        public void addTimer(final String timerName, final String tickPolicy) {
            timers.add(new Timer(timerName, tickPolicy, true, false));
        }

        public enum ClockOffset {
            UTC("utc"), LOCALTIME("localtime"), TIMEZONE("timezone"), VARIABLE("variable");

            private final String offset;

            ClockOffset(final String offset) {
                this.offset = offset;
            }

            @Override
            public String toString() {
                return this.offset;
            }
        }

        @Override
        public String toString() {
            final StringBuilder clockBuilder = new StringBuilder();
            clockBuilder.append("<clock offset='");
            clockBuilder.append(this.offset.toString());
            clockBuilder.append("'>\n");
            this.timers.forEach(timer -> clockBuilder.append(timer.toString()));
            clockBuilder.append("</clock>\n");
            return clockBuilder.toString();
        }
    }

    public static class DevicesDef {
        private final Map<String, List<?>> devices = new HashMap<>();
        private String emulator;
        private GuestType guestType;

        public boolean addDevice(final Object device) {
            final Object dev = this.devices.get(device.getClass().toString());
            if (dev == null) {
                final List<Object> devs = new ArrayList<>();
                devs.add(device);
                this.devices.put(device.getClass().toString(), devs);
            } else {
                final List<Object> devs = (List<Object>) dev;
                devs.add(device);
            }
            return true;
        }

        public void setEmulatorPath(final String emulator) {
            this.emulator = emulator;
        }

        public void setGuestType(final GuestType guestType) {
            this.guestType = guestType;
        }

        public List<LibvirtDiskDef> getDisks() {
            return (List<LibvirtDiskDef>) this.devices.get(LibvirtDiskDef.class.toString());
        }

        @Override
        public String toString() {
            final StringBuilder devicesBuilder = new StringBuilder();
            devicesBuilder.append("<devices>\n");
            if (this.emulator != null) {
                devicesBuilder.append("<emulator>" + this.emulator + "</emulator>\n");
            }

            for (final List<?> devs : this.devices.values()) {
                for (final Object dev : devs) {
                    devicesBuilder.append(dev.toString());
                }
            }
            devicesBuilder.append("</devices>\n");
            return devicesBuilder.toString();
        }

        public List<InterfaceDef> getInterfaces() {
            return (List<InterfaceDef>) this.devices.get(InterfaceDef.class.toString());
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
            this.netType = GuestNetType.BRIDGE;
            this.sourceName = brName;
            this.networkName = targetBrName;
            this.macAddr = macAddr;
            this.model = model;
            this.networkRateKBps = networkRateKBps;
        }

        public void defDirectNet(final String sourceName, final String targetName, final String macAddr, final NicModel model, final String sourceMode) {
            defDirectNet(sourceName, targetName, macAddr, model, sourceMode, 0);
        }

        public void defDirectNet(final String sourceName, final String targetName, final String macAddr, final NicModel model, final String sourceMode,
                                 final Integer networkRateKBps) {
            this.netType = GuestNetType.DIRECT;
            this.netSourceMode = sourceMode;
            this.sourceName = sourceName;
            this.networkName = targetName;
            this.macAddr = macAddr;
            this.model = model;
            this.networkRateKBps = networkRateKBps;
        }

        public void defPrivateNet(final String networkName, final String targetName, final String macAddr, final NicModel model) {
            defPrivateNet(networkName, targetName, macAddr, model, 0);
        }

        public void defPrivateNet(String networkName, final String targetName, final String macAddr, final NicModel model,
                                  final Integer networkRateKBps) {
            this.netType = GuestNetType.NETWORK;
            this.sourceName = networkName;
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
            this.netType = GuestNetType.ETHERNET;
            this.networkName = targetName;
            this.sourceName = targetName;
            this.macAddr = macAddr;
            this.model = model;
            this.scriptPath = scriptPath;
            this.networkRateKBps = networkRateKBps;
        }

        public HostNicType getHostNetType() {
            return this.hostNetType;
        }

        public void setHostNetType(final HostNicType hostNetType) {
            this.hostNetType = hostNetType;
        }

        public String getBrName() {
            return this.sourceName;
        }

        public GuestNetType getNetType() {
            return this.netType;
        }

        public String getNetSourceMode() {
            return this.netSourceMode;
        }

        public String getDevName() {
            return this.networkName;
        }

        public String getMacAddress() {
            return this.macAddr;
        }

        public NicModel getModel() {
            return this.model;
        }

        public String getVirtualPortType() {
            return this.virtualPortType;
        }

        public void setVirtualPortType(final String virtualPortType) {
            this.virtualPortType = virtualPortType;
        }

        public String getVirtualPortInterfaceId() {
            return this.virtualPortInterfaceId;
        }

        public void setVirtualPortInterfaceId(final String virtualPortInterfaceId) {
            this.virtualPortInterfaceId = virtualPortInterfaceId;
        }

        public int getVlanTag() {
            return this.vlanTag;
        }

        public void setVlanTag(final int vlanTag) {
            this.vlanTag = vlanTag;
        }

        enum HostNicType {
            DIRECT_ATTACHED_WITHOUT_DHCP, DIRECT_ATTACHED_WITH_DHCP, VNET, VLAN
        }

        @Override
        public String toString() {
            final StringBuilder netBuilder = new StringBuilder();
            netBuilder.append("<interface type='" + this.netType + "'>\n");
            if (this.netType == GuestNetType.BRIDGE) {
                netBuilder.append("<source bridge='" + this.sourceName + "'/>\n");
            } else if (this.netType == GuestNetType.NETWORK) {
                netBuilder.append("<source network='" + this.sourceName + "'/>\n");
            } else if (this.netType == GuestNetType.DIRECT) {
                netBuilder.append("<source dev='" + this.sourceName + "' mode='" + this.netSourceMode + "'/>\n");
            }
            if (this.networkName != null) {
                netBuilder.append("<target dev='" + this.networkName + "'/>\n");
            }
            if (this.macAddr != null) {
                netBuilder.append("<mac address='" + this.macAddr + "'/>\n");
            }
            if (this.model != null) {
                netBuilder.append("<model type='" + this.model + "'/>\n");
            }
            if (s_libvirtVersion >= 9004 && this.networkRateKBps > 0) { // supported from libvirt 0.9.4
                netBuilder.append("<bandwidth>\n");
                netBuilder.append("<inbound average='" + this.networkRateKBps + "' peak='" + this.networkRateKBps + "'/>\n");
                netBuilder.append("<outbound average='" + this.networkRateKBps + "' peak='" + this.networkRateKBps + "'/>\n");
                netBuilder.append("</bandwidth>\n");
            }
            if (this.scriptPath != null) {
                netBuilder.append("<script path='" + this.scriptPath + "'/>\n");
            }
            if (this.virtualPortType != null) {
                netBuilder.append("<virtualport type='" + this.virtualPortType + "'>\n");
                if (this.virtualPortInterfaceId != null) {
                    netBuilder.append("<parameters interfaceid='" + this.virtualPortInterfaceId + "'/>\n");
                }
                netBuilder.append("</virtualport>\n");
            }
            if (this.vlanTag > 0 && this.vlanTag < 4095) {
                netBuilder.append("<vlan trunk='no'>\n<tag id='" + this.vlanTag + "'/>\n</vlan>");
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
            this.ttyPath = path;
            this.source = source;
            this.port = port;
        }

        @Override
        public String toString() {
            final StringBuilder consoleBuilder = new StringBuilder();
            consoleBuilder.append("<console ");
            consoleBuilder.append("type='" + this.type + "'");
            if (this.ttyPath != null) {
                consoleBuilder.append("tty='" + this.ttyPath + "'");
            }
            consoleBuilder.append(">\n");
            if (this.source != null) {
                consoleBuilder.append("<source path='" + this.source + "'/>\n");
            }
            if (this.port != -1) {
                consoleBuilder.append("<target port='" + this.port + "'/>\n");
            }
            consoleBuilder.append("</console>\n");
            return consoleBuilder.toString();
        }
    }

    public static class CpuTuneDef {
        private int shares = 0;

        public int getShares() {
            return this.shares;
        }

        public void setShares(final int shares) {
            this.shares = shares;
        }

        @Override
        public String toString() {
            final StringBuilder cpuTuneBuilder = new StringBuilder();
            cpuTuneBuilder.append("<cputune>\n");
            if (this.shares > 0) {
                cpuTuneBuilder.append("<shares>" + this.shares + "</shares>\n");
            }
            cpuTuneBuilder.append("</cputune>\n");
            return cpuTuneBuilder.toString();
        }
    }

    public static class CpuModeDef {
        private String mode;
        private String model;
        private String cpuflags;
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

        public void setCpuflags(final String cpuflags) {
            this.cpuflags = cpuflags;
        }

        public void setTopology(final int coresPerSocket, final int sockets) {
            this.coresPerSocket = coresPerSocket;
            this.sockets = sockets;
        }

        @Override
        public String toString() {
            final StringBuilder modeBuilder = new StringBuilder();
            final Map<String, Boolean> featureMap = new HashMap<>();

            // start cpu def, adding mode, model
            if ("custom".equalsIgnoreCase(this.mode) && this.model != null) {
                modeBuilder.append("<cpu mode='custom' match='exact'><model fallback='allow'>" + this.model + "</model>");
            } else if ("host-model".equals(this.mode)) {
                modeBuilder.append("<cpu mode='host-model'><model fallback='allow'></model>");
            } else if ("host-passthrough".equals(this.mode)) {
                modeBuilder.append("<cpu mode='host-passthrough'>");
            } else {
                modeBuilder.append("<cpu>");
            }

            // First process agent.properties cpuflags features
            if (this.features != null) {
                for (final String feature : this.features) {
                    if (feature.startsWith("-")) {
                        featureMap.put(feature.substring(1), Boolean.FALSE);
                    } else {
                        featureMap.put(feature, Boolean.TRUE);
                    }
                }
            }

            // Secondly process guest_os cpuflags which overrides the agent.properties flags
            if (this.cpuflags != null && !this.cpuflags.isEmpty()) {
                for (final String flag : this.cpuflags.split(" ")) {
                    if (flag.startsWith("-")) {
                        featureMap.put(flag.substring(1), Boolean.FALSE);
                    } else {
                        featureMap.put(flag, Boolean.TRUE);
                    }
                }
            }

            if (this.features != null || this.cpuflags != null) {
                for (final String feature : featureMap.keySet()) {
                    if (featureMap.get(feature)) {
                        modeBuilder.append("<feature policy='require' name='" + feature + "'/>");
                    } else {
                        modeBuilder.append("<feature policy='disable' name='" + feature + "'/>");
                    }
                }
            }

            // add topology
            if (this.sockets > 0 && this.coresPerSocket > 0) {
                modeBuilder.append("<topology sockets='" + this.sockets + "' cores='" + this.coresPerSocket + "' threads='1' />");
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
            serialBuidler.append("<serial type='" + this.type + "'>\n");
            if (this.source != null) {
                serialBuidler.append("<source path='" + this.source + "'/>\n");
            }
            if (this.port != -1) {
                serialBuidler.append("<target port='" + this.port + "'/>\n");
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
            if (this.videoModel != null && !this.videoModel.isEmpty() && this.videoRam != 0) {
                videoBuilder.append("<video>\n");
                videoBuilder.append("<model type='" + this.videoModel + "' vram='" + this.videoRam + "'/>\n");
                videoBuilder.append("</video>\n");
                return videoBuilder.toString();
            }
            return "";
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
        private final String passwd;
        private final String keyMap;
        private short port = -2;
        private boolean autoPort = false;

        public GraphicDef(final String type, final short port, final boolean autoPort, final String passwd, final String keyMap) {
            this.type = type;
            this.port = port;
            this.autoPort = autoPort;
            this.passwd = StringEscapeUtils.escapeXml(passwd);
            this.keyMap = keyMap;
        }

        @Override
        public String toString() {
            final StringBuilder graphicBuilder = new StringBuilder();
            graphicBuilder.append("<graphics type='" + this.type + "'");
            if (this.autoPort) {
                graphicBuilder.append(" autoport='yes'");
            } else if (this.port != -2) {
                graphicBuilder.append(" port='" + this.port + "'");
            }

            graphicBuilder.append(" listen='0.0.0.0'");

            if (this.passwd != null) {
                graphicBuilder.append(" passwd='" + this.passwd + "'");
            } else if (this.keyMap != null) {
                graphicBuilder.append(" _keymap='" + this.keyMap + "'");
            }
            graphicBuilder.append("/>\n");
            return graphicBuilder.toString();
        }
    }

    public static class ScsiDef {
        private short index = 0;
        private int domain = 0;
        private int bus = 0;
        private int slot = 9;
        private int function = 0;

        public ScsiDef(final short index, final int domain, final int bus, final int slot, final int function) {
            this.index = index;
            this.domain = domain;
            this.bus = bus;
            this.slot = slot;
            this.function = function;
        }

        public ScsiDef() {

        }

        @Override
        public String toString() {
            final StringBuilder scsiBuilder = new StringBuilder();

            scsiBuilder.append(String.format("<controller type='scsi' index='%d' model='virtio-scsi'>\n", this.index));
            scsiBuilder.append(String.format("<address type='pci' domain='0x%04X' bus='0x%02X' slot='0x%02X' function='0x%01X'/>\n",
                    this.domain, this.bus, this.slot, this.function));
            scsiBuilder.append("</controller>\n");
            return scsiBuilder.toString();
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
            inputBuilder.append("<input type='" + this.type + "'");
            if (this.bus != null) {
                inputBuilder.append(" bus='" + this.bus + "'");
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
            fsBuilder.append("  <source dir='" + this.sourcePath + "'/>\n");
            fsBuilder.append("  <target dir='" + this.targetPath + "'/>\n");
            fsBuilder.append("</filesystem>\n");
            return fsBuilder.toString();
        }
    }

    public static class MetadataDef {
        private final Map<String, Object> nodes = new HashMap<>();

        public Map<String, Object> getNodes() {
            return this.nodes;
        }

        @Override
        public String toString() {
            final StringBuilder xmlBuilder = new StringBuilder();
            xmlBuilder.append("<metadata>");
            xmlBuilder.append("<cosmic:metadata xmlns:cosmic=\"https://github.com/MissionCriticalCloud\">");
            for (final Map.Entry<String, Object> entry : this.nodes.entrySet()) {
                xmlBuilder.append("<cosmic:");
                xmlBuilder.append(entry.getKey());
                xmlBuilder.append(">");
                xmlBuilder.append(entry.getValue());
                xmlBuilder.append("</cosmic:");
                xmlBuilder.append(entry.getKey());
                xmlBuilder.append(">");
            }
            xmlBuilder.append("</cosmic:metadata>");
            xmlBuilder.append("</metadata>");
            return xmlBuilder.toString();
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
            return this.path;
        }

        public RngBackendModel getRngBackendModel() {
            return this.rngBackendModel;
        }

        public RngModel getRngModel() {
            return this.rngModel;
        }

        @Override
        public String toString() {
            final StringBuilder rngBuilder = new StringBuilder();
            rngBuilder.append("<rng model='" + this.rngModel + "'>\n");
            rngBuilder.append("<backend model='" + this.rngBackendModel + "'>" + this.path + "</backend>");
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
            return this.action;
        }

        public WatchDogModel getModel() {
            return this.model;
        }

        @Override
        public String toString() {
            final StringBuilder wacthDogBuilder = new StringBuilder();
            wacthDogBuilder.append("<watchdog model='" + this.model + "' action='" + this.action + "'/>\n");
            return wacthDogBuilder.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder vmBuilder = new StringBuilder();
        vmBuilder.append("<domain type='" + this.hvsType + "'>\n");
        vmBuilder.append("<name>" + this.domName + "</name>\n");
        if (this.domUuid != null) {
            vmBuilder.append("<uuid>" + this.domUuid + "</uuid>\n");
        }
        if (this.desc != null) {
            vmBuilder.append("<description>" + this.desc + "</description>\n");
        }
        for (final Object o : this.components.values()) {
            vmBuilder.append(o.toString());
        }
        vmBuilder.append("</domain>\n");
        return vmBuilder.toString();
    }
}
