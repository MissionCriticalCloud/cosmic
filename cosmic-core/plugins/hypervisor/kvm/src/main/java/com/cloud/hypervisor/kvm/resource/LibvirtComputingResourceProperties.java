package com.cloud.hypervisor.kvm.resource;

import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_CMDS_TIMEOUT;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_DOMR_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_HOST_RESERVED_MEM_MB;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_HYPERVISOR_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_HYPERVISOR_TYPE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_IPADDR_START;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_LOCAL_STORAGE_PATH;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_MOUNT_PATH;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_NETWORK_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_PRIVATE_BRIDGE_NAME;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_PRIVATE_MACADDR_START;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_PRIVATE_NETWORK_DEVICE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_PRIVATE_NETWORK_NAME;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_PUBLIC_NETWORK_DEVICE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_SCRIPTS_TIMEOUT;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_STOP_SCRIPT_TIMEOUT;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_STORAGE_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_SYSTEMVM_ISO_PATH;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_MIGRATE_DOWNTIME;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_MIGRATE_PAUSEAFTER;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_MIGRATE_SPEED;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_RNG_PATH;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_VIDEO_RAM;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.FORMAT_PRIVATE_BRIDGE_NAME;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.FORMAT_PRIVATE_NETWORK_NAME;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_CMDS_TIMEOUT;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_DOMR_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_CPU_FEATURES;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_CPU_MODE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_CPU_MODEL;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_NETWORK_DEVICE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_HOST_RESERVED_MEM_MB;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_HYPERVISOR_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_HYPERVISOR_TYPE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_HYPERVISOR_URI;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_KVMCLOCK_DISABLE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_KVM_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_LOCAL_STORAGE_PATH;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_LOCAL_STORAGE_UUID;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_MOUNT_PATH;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_NETWORK_BRIDGE_TYPE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_NETWORK_DIRECT_DEVICE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_NETWORK_DIRECT_SOURCE_MODE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_NETWORK_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_BRIDGE_NAME;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_IPADDR_START;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_MACADDR_START;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_NETWORK_DEVICE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_NETWORK_NAME;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PUBLIC_NETWORK_DEVICE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_SCRIPTS_TIMEOUT;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_STOP_SCRIPT_TIMEOUT;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_STORAGE_SCRIPTS_DIR;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_SYSTEMVM_ISO_PATH;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MEMBALLOON_DISABLE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MIGRATE_DOWNTIME;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MIGRATE_PAUSEAFTER;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MIGRATE_SPEED;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_RNG_ENABLE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_RNG_MODEL;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_RNG_PATH;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_VIDEO_HARDWARE;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_VIDEO_RAM;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_WATCHDOG_ACTION;
import static com.cloud.hypervisor.kvm.resource.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_WATCHDOG_MODEL;
import static com.cloud.utils.CloudConstants.DEFAULT_HOST;
import static com.cloud.utils.CloudConstants.DEFAULT_POD;
import static com.cloud.utils.CloudConstants.DEFAULT_ZONE;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_CLUSTER;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_HOST;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_INSTANCE;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_POD;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_POOL;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_ZONE;
import static com.cloud.utils.PropertiesUtil.parse;
import static com.cloud.utils.PropertiesUtil.stringSplitDecomposer;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource.BridgeType;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.RngDef.RngBackendModel;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.WatchDogDef.WatchDogAction;
import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.WatchDogDef.WatchDogModel;
import com.cloud.utils.PropertiesPojo;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class LibvirtComputingResourceProperties implements PropertiesPojo {
    private String cluster;
    private int cmdsTimeout = DEFAULT_CMDS_TIMEOUT;
    private String domrScriptsDir = DEFAULT_DOMR_SCRIPTS_DIR;
    private boolean developer = false;
    private List<String> guestCpuFeatures;
    private String guestCpuModel;
    private String guestCpuMode;
    private String guestNetworkDevice;
    private String host = DEFAULT_HOST;
    private long hostReservedMemMb = DEFAULT_HOST_RESERVED_MEM_MB;
    private String hypervisorScriptsDir = DEFAULT_HYPERVISOR_SCRIPTS_DIR;
    private HypervisorType hypervisorType = DEFAULT_HYPERVISOR_TYPE;
    private String hypervisorUri;
    private String instance;
    private String kvmScriptsDir;
    private boolean kvmclockDisable;
    private String localStoragePath = DEFAULT_LOCAL_STORAGE_PATH;
    private String localStorageUuid;
    private String mountPath = DEFAULT_MOUNT_PATH;
    private String networkScriptsDir = DEFAULT_NETWORK_SCRIPTS_DIR;
    private BridgeType networkBridgeType = BridgeType.NATIVE;
    private String networkDirectSourceMode;
    private String networkDirectDevice;
    private String pool;
    private String pod = DEFAULT_POD;
    private String privateMacaddrStart = DEFAULT_PRIVATE_MACADDR_START;
    private String privateIpaddrStart = DEFAULT_IPADDR_START;
    private String privateBridgeName = DEFAULT_PRIVATE_BRIDGE_NAME;
    private String publicNetworkDevice = DEFAULT_PUBLIC_NETWORK_DEVICE;
    private String privateNetworkDevice = DEFAULT_PRIVATE_NETWORK_DEVICE;
    private String privateNetworkName = DEFAULT_PRIVATE_NETWORK_NAME;
    private String storageScriptsDir = DEFAULT_STORAGE_SCRIPTS_DIR;
    private int scriptsTimeout = DEFAULT_SCRIPTS_TIMEOUT;
    private int stopScriptTimeout = DEFAULT_STOP_SCRIPT_TIMEOUT;
    private String systemvmIsoPath = DEFAULT_SYSTEMVM_ISO_PATH;
    private boolean vmMemballoonDisable;
    private String vmVideoHardware;
    private int vmVideoRam = DEFAULT_VM_VIDEO_RAM;
    private boolean vmRngEnable = false;
    private RngBackendModel vmRngModel = RngBackendModel.RANDOM;
    private String vmRngPath = DEFAULT_VM_RNG_PATH;
    private WatchDogModel vmWatchdogModel = WatchDogModel.I6300ESB;
    private WatchDogAction vmWatchdogAction = WatchDogAction.NONE;
    private int vmMigrateDowntime = DEFAULT_VM_MIGRATE_DOWNTIME;
    private int vmMigratePauseafter = DEFAULT_VM_MIGRATE_PAUSEAFTER;
    private int vmMigrateSpeed = DEFAULT_VM_MIGRATE_SPEED;
    private String zone = DEFAULT_ZONE;

    @Override
    public void load(final Properties properties) {
        cluster = parse(properties, PROPERTY_KEY_CLUSTER, cluster);
        cmdsTimeout = parse(properties, PROPERTY_KEY_CMDS_TIMEOUT, cmdsTimeout);
        domrScriptsDir = parse(properties, PROPERTY_KEY_DOMR_SCRIPTS_DIR, domrScriptsDir);
        developer = parse(properties, PROPERTY_KEY_DOMR_SCRIPTS_DIR, developer);
        guestCpuFeatures = parse(properties, PROPERTY_KEY_GUEST_CPU_FEATURES, guestCpuFeatures, stringSplitDecomposer(" ", String.class));
        guestCpuModel = parse(properties, PROPERTY_KEY_GUEST_CPU_MODEL, guestCpuModel);
        guestCpuMode = parse(properties, PROPERTY_KEY_GUEST_CPU_MODE, guestCpuMode);
        guestNetworkDevice = parse(properties, PROPERTY_KEY_GUEST_NETWORK_DEVICE, guestNetworkDevice);
        host = parse(properties, PROPERTY_KEY_HOST, host);
        hostReservedMemMb = parse(properties, PROPERTY_KEY_HOST_RESERVED_MEM_MB, hostReservedMemMb);
        hypervisorScriptsDir = parse(properties, PROPERTY_KEY_HYPERVISOR_SCRIPTS_DIR, hypervisorScriptsDir);
        hypervisorUri = parse(properties, PROPERTY_KEY_HYPERVISOR_URI, hypervisorUri);
        instance = parse(properties, PROPERTY_KEY_INSTANCE, instance);
        kvmScriptsDir = parse(properties, PROPERTY_KEY_KVM_SCRIPTS_DIR, kvmScriptsDir);
        kvmclockDisable = parse(properties, PROPERTY_KEY_KVMCLOCK_DISABLE, kvmclockDisable);
        localStoragePath = parse(properties, PROPERTY_KEY_LOCAL_STORAGE_PATH, localStoragePath);
        localStorageUuid = parse(properties, PROPERTY_KEY_LOCAL_STORAGE_UUID, localStorageUuid);
        mountPath = parse(properties, PROPERTY_KEY_MOUNT_PATH, mountPath);
        networkScriptsDir = parse(properties, PROPERTY_KEY_NETWORK_SCRIPTS_DIR, networkScriptsDir);
        networkDirectSourceMode = parse(properties, PROPERTY_KEY_NETWORK_DIRECT_SOURCE_MODE, networkDirectSourceMode);
        networkDirectDevice = parse(properties, PROPERTY_KEY_NETWORK_DIRECT_DEVICE, networkDirectDevice);
        pool = parse(properties, PROPERTY_KEY_POOL, pool);
        pod = parse(properties, PROPERTY_KEY_POD, pod);
        privateMacaddrStart = parse(properties, PROPERTY_KEY_PRIVATE_MACADDR_START, privateMacaddrStart);
        privateIpaddrStart = parse(properties, PROPERTY_KEY_PRIVATE_IPADDR_START, privateIpaddrStart);
        privateBridgeName = parse(properties, PROPERTY_KEY_PRIVATE_BRIDGE_NAME, privateBridgeName);
        publicNetworkDevice = parse(properties, PROPERTY_KEY_PUBLIC_NETWORK_DEVICE, publicNetworkDevice);
        privateNetworkDevice = parse(properties, PROPERTY_KEY_PRIVATE_NETWORK_DEVICE, privateNetworkDevice);
        privateNetworkName = parse(properties, PROPERTY_KEY_PRIVATE_NETWORK_NAME, privateNetworkName);
        storageScriptsDir = parse(properties, PROPERTY_KEY_STORAGE_SCRIPTS_DIR, storageScriptsDir);
        scriptsTimeout = parse(properties, PROPERTY_KEY_SCRIPTS_TIMEOUT, scriptsTimeout);
        stopScriptTimeout = parse(properties, PROPERTY_KEY_STOP_SCRIPT_TIMEOUT, stopScriptTimeout);
        systemvmIsoPath = parse(properties, PROPERTY_KEY_SYSTEMVM_ISO_PATH, systemvmIsoPath);
        vmMemballoonDisable = parse(properties, PROPERTY_KEY_VM_MEMBALLOON_DISABLE, vmMemballoonDisable);
        vmVideoHardware = parse(properties, PROPERTY_KEY_VM_VIDEO_HARDWARE, vmVideoHardware);
        vmVideoRam = parse(properties, PROPERTY_KEY_VM_VIDEO_RAM, vmVideoRam);
        vmRngEnable = parse(properties, PROPERTY_KEY_VM_RNG_ENABLE, vmRngEnable);
        vmRngPath = parse(properties, PROPERTY_KEY_VM_RNG_PATH, vmRngPath);
        vmMigrateDowntime = parse(properties, PROPERTY_KEY_VM_MIGRATE_DOWNTIME, vmMigrateDowntime);
        vmMigratePauseafter = parse(properties, PROPERTY_KEY_VM_MIGRATE_PAUSEAFTER, vmMigratePauseafter);
        vmMigrateSpeed = parse(properties, PROPERTY_KEY_VM_MIGRATE_SPEED, vmMigrateSpeed);
        zone = parse(properties, PROPERTY_KEY_ZONE, zone);

        hypervisorType = parse(properties, PROPERTY_KEY_HYPERVISOR_TYPE, hypervisorType, HypervisorType.class);
        networkBridgeType = parse(properties, PROPERTY_KEY_NETWORK_BRIDGE_TYPE, networkBridgeType, BridgeType.class);
        vmRngModel = parse(properties, PROPERTY_KEY_VM_RNG_MODEL, vmRngModel, RngBackendModel.class);
        vmWatchdogModel = parse(properties, PROPERTY_KEY_VM_WATCHDOG_MODEL, vmWatchdogModel, WatchDogModel.class);
        vmWatchdogAction = parse(properties, PROPERTY_KEY_VM_WATCHDOG_ACTION, vmWatchdogAction, WatchDogAction.class);

        validateValues();
    }

    private void validateValues() {
        hypervisorType = hypervisorType == HypervisorType.None ? HypervisorType.KVM : hypervisorType;
        hypervisorUri = hypervisorUri == null ? LibvirtConnection.getHypervisorUri(hypervisorType.toString()) : hypervisorUri;
        privateBridgeName = developer ? String.format(FORMAT_PRIVATE_BRIDGE_NAME, instance) : privateBridgeName;
        guestNetworkDevice = guestNetworkDevice == null ? privateNetworkDevice : guestNetworkDevice;
        privateNetworkName = developer ? String.format(FORMAT_PRIVATE_NETWORK_NAME, instance) : privateNetworkName;
        localStoragePath = new File(localStoragePath).getAbsolutePath();
        localStorageUuid = localStorageUuid == null ? UUID.randomUUID().toString() : localStorageUuid;
    }

    @Override
    public Map<String, Object> buildPropertiesMap() {
        final HashMap<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put(PROPERTY_KEY_CLUSTER, cluster);
        propertiesMap.put(PROPERTY_KEY_CMDS_TIMEOUT, cmdsTimeout);
        propertiesMap.put(PROPERTY_KEY_DOMR_SCRIPTS_DIR, domrScriptsDir);
        propertiesMap.put(PROPERTY_KEY_GUEST_CPU_FEATURES, guestCpuFeatures);
        propertiesMap.put(PROPERTY_KEY_GUEST_CPU_MODEL, guestCpuModel);
        propertiesMap.put(PROPERTY_KEY_GUEST_CPU_MODE, guestCpuMode);
        propertiesMap.put(PROPERTY_KEY_GUEST_NETWORK_DEVICE, guestNetworkDevice);
        propertiesMap.put(PROPERTY_KEY_HOST, host);
        propertiesMap.put(PROPERTY_KEY_HOST_RESERVED_MEM_MB, hostReservedMemMb);
        propertiesMap.put(PROPERTY_KEY_HYPERVISOR_SCRIPTS_DIR, hypervisorScriptsDir);
        propertiesMap.put(PROPERTY_KEY_HYPERVISOR_TYPE, hypervisorType);
        propertiesMap.put(PROPERTY_KEY_HYPERVISOR_URI, hypervisorUri);
        propertiesMap.put(PROPERTY_KEY_INSTANCE, instance);
        propertiesMap.put(PROPERTY_KEY_KVM_SCRIPTS_DIR, kvmScriptsDir);
        propertiesMap.put(PROPERTY_KEY_KVMCLOCK_DISABLE, kvmclockDisable);
        propertiesMap.put(PROPERTY_KEY_LOCAL_STORAGE_PATH, localStoragePath);
        propertiesMap.put(PROPERTY_KEY_LOCAL_STORAGE_UUID, localStorageUuid);
        propertiesMap.put(PROPERTY_KEY_MOUNT_PATH, mountPath);
        propertiesMap.put(PROPERTY_KEY_NETWORK_SCRIPTS_DIR, networkScriptsDir);
        propertiesMap.put(PROPERTY_KEY_NETWORK_BRIDGE_TYPE, networkBridgeType);
        propertiesMap.put(PROPERTY_KEY_NETWORK_DIRECT_SOURCE_MODE, networkDirectSourceMode);
        propertiesMap.put(PROPERTY_KEY_NETWORK_DIRECT_DEVICE, networkDirectDevice);
        propertiesMap.put(PROPERTY_KEY_POOL, pool);
        propertiesMap.put(PROPERTY_KEY_POD, pod);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_MACADDR_START, privateMacaddrStart);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_IPADDR_START, privateIpaddrStart);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_BRIDGE_NAME, privateBridgeName);
        propertiesMap.put(PROPERTY_KEY_PUBLIC_NETWORK_DEVICE, publicNetworkDevice);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_NETWORK_DEVICE, privateNetworkDevice);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_NETWORK_NAME, privateNetworkName);
        propertiesMap.put(PROPERTY_KEY_STORAGE_SCRIPTS_DIR, storageScriptsDir);
        propertiesMap.put(PROPERTY_KEY_SCRIPTS_TIMEOUT, scriptsTimeout);
        propertiesMap.put(PROPERTY_KEY_STOP_SCRIPT_TIMEOUT, stopScriptTimeout);
        propertiesMap.put(PROPERTY_KEY_SYSTEMVM_ISO_PATH, systemvmIsoPath);
        propertiesMap.put(PROPERTY_KEY_VM_MEMBALLOON_DISABLE, vmMemballoonDisable);
        propertiesMap.put(PROPERTY_KEY_VM_VIDEO_HARDWARE, vmVideoHardware);
        propertiesMap.put(PROPERTY_KEY_VM_VIDEO_RAM, vmVideoRam);
        propertiesMap.put(PROPERTY_KEY_VM_RNG_ENABLE, vmRngEnable);
        propertiesMap.put(PROPERTY_KEY_VM_RNG_MODEL, vmRngModel);
        propertiesMap.put(PROPERTY_KEY_VM_RNG_PATH, vmRngPath);
        propertiesMap.put(PROPERTY_KEY_VM_WATCHDOG_MODEL, vmWatchdogModel);
        propertiesMap.put(PROPERTY_KEY_VM_WATCHDOG_ACTION, vmWatchdogAction);
        propertiesMap.put(PROPERTY_KEY_VM_MIGRATE_DOWNTIME, vmMigrateDowntime);
        propertiesMap.put(PROPERTY_KEY_VM_MIGRATE_PAUSEAFTER, vmMigratePauseafter);
        propertiesMap.put(PROPERTY_KEY_VM_MIGRATE_SPEED, vmMigrateSpeed);
        propertiesMap.put(PROPERTY_KEY_ZONE, zone);
        return propertiesMap;
    }

    public String getCluster() {
        return cluster;
    }

    public int getCmdsTimeout() {
        return cmdsTimeout * 1000;
    }

    public String getDomrScriptsDir() {
        return domrScriptsDir;
    }

    public String getGuestCpuModel() {
        return guestCpuModel;
    }

    public String getGuestCpuMode() {
        return guestCpuMode;
    }

    public String getGuestNetworkDevice() {
        return guestNetworkDevice;
    }

    public String getHost() {
        return host;
    }

    public long getHostReservedMemMb() {
        return hostReservedMemMb * 1024 * 1024L;
    }

    public String getHypervisorScriptsDir() {
        return hypervisorScriptsDir;
    }

    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    public String getHypervisorUri() {
        return hypervisorUri;
    }

    public String getKvmScriptsDir() {
        return kvmScriptsDir;
    }

    public boolean isKvmclockDisable() {
        return kvmclockDisable;
    }

    public String getLocalStoragePath() {
        return localStoragePath;
    }

    public String getLocalStorageUuid() {
        return localStorageUuid;
    }

    public String getMountPath() {
        return mountPath;
    }

    public String getNetworkScriptsDir() {
        return networkScriptsDir;
    }

    public BridgeType getNetworkBridgeType() {
        return networkBridgeType;
    }

    public String getNetworkDirectSourceMode() {
        return networkDirectSourceMode;
    }

    public String getNetworkDirectDevice() {
        return networkDirectDevice;
    }

    public String getPod() {
        return pod;
    }

    public String getPrivateMacaddrStart() {
        return privateMacaddrStart;
    }

    public String getPrivateIpaddrStart() {
        return privateIpaddrStart;
    }

    public String getPrivateBridgeName() {
        return privateBridgeName;
    }

    public String getPublicNetworkDevice() {
        return publicNetworkDevice;
    }

    public String getPrivateNetworkDevice() {
        return privateNetworkDevice;
    }

    public String getPrivateNetworkName() {
        return privateNetworkName;
    }

    public String getStorageScriptsDir() {
        return storageScriptsDir;
    }

    public int getScriptsTimeout() {
        return scriptsTimeout * 1000;
    }

    public int getStopScriptTimeout() {
        return stopScriptTimeout * 1000;
    }

    public String getSystemvmIsoPath() {
        return systemvmIsoPath;
    }

    public boolean getVmMemballoonDisable() {
        return vmMemballoonDisable;
    }

    public String getVmVideoHardware() {
        return vmVideoHardware;
    }

    public int getVmVideoRam() {
        return vmVideoRam;
    }

    public boolean getVmRngEnable() {
        return vmRngEnable;
    }

    public RngBackendModel getVmRngModel() {
        return vmRngModel;
    }

    public String getVmRngPath() {
        return vmRngPath;
    }

    public WatchDogModel getVmWatchdogModel() {
        return vmWatchdogModel;
    }

    public WatchDogAction getVmWatchdogAction() {
        return vmWatchdogAction;
    }

    public int getVmMigrateDowntime() {
        return vmMigrateDowntime;
    }

    public int getVmMigratePauseafter() {
        return vmMigratePauseafter;
    }

    public int getVmMigrateSpeed() {
        return vmMigrateSpeed;
    }

    public List<String> getGuestCpuFeatures() {
        return guestCpuFeatures;
    }

    public String getInstance() {
        return instance;
    }

    public String getPool() {
        return pool;
    }

    public boolean isVmMemballoonDisable() {
        return vmMemballoonDisable;
    }

    public boolean isVmRngEnable() {
        return vmRngEnable;
    }

    public String getZone() {
        return zone;
    }

    public boolean isDeveloper() {
        return developer;
    }

    public boolean hasGuestCpuMode() {
        return guestCpuMode != null;
    }

    public void unsetGuestCpuMode() {
        guestCpuMode = "";
    }

    public void unsetGuestCpuModel() {
        guestCpuModel = "";
    }

    public void setVmMigrateSpeed(final int vmMigrateSpeed) {
        this.vmMigrateSpeed = vmMigrateSpeed;
    }

    public void setNetworkBridgeType(final BridgeType bridgeType) {
        this.networkBridgeType = bridgeType;
    }

    public static class Constants {
        public static final String BRIDGE_LINKLOCAL = "linklocal";
        public static final String BRIDGE_PUBLIC = "public";
        public static final String BRIDGE_PRIVATE = "private";
        public static final String BRIDGE_GUEST = "guest";

        public static final String PROPERTY_KEY_DOMR_SCRIPTS_DIR = "domr.scripts.dir";
        public static final String PROPERTY_KEY_HYPERVISOR_SCRIPTS_DIR = "hypervisor.scripts.dir";
        public static final String PROPERTY_KEY_KVM_SCRIPTS_DIR = "kvm.scripts.dir";
        public static final String PROPERTY_KEY_NETWORK_SCRIPTS_DIR = "network.scripts.dir";
        public static final String PROPERTY_KEY_STORAGE_SCRIPTS_DIR = "storage.scripts.dir";
        public static final String PROPERTY_KEY_NETWORK_BRIDGE_TYPE = "network.bridge.type";
        public static final String PROPERTY_KEY_HYPERVISOR_TYPE = "hypervisor.type";
        public static final String PROPERTY_KEY_HYPERVISOR_URI = "hypervisor.uri";
        public static final String PROPERTY_KEY_NETWORK_DIRECT_SOURCE_MODE = "network.direct.source.mode";
        public static final String PROPERTY_KEY_NETWORK_DIRECT_DEVICE = "network.direct.device";
        public static final String PROPERTY_KEY_PRIVATE_MACADDR_START = "private.macaddr.start";
        public static final String PROPERTY_KEY_PRIVATE_IPADDR_START = "private.ipaddr.start";
        public static final String PROPERTY_KEY_PRIVATE_BRIDGE_NAME = "private.bridge.name";
        public static final String PROPERTY_KEY_PUBLIC_NETWORK_DEVICE = "public.network.device";
        public static final String PROPERTY_KEY_PRIVATE_NETWORK_DEVICE = "private.network.device";
        public static final String PROPERTY_KEY_GUEST_NETWORK_DEVICE = "guest.network.device";
        public static final String PROPERTY_KEY_PRIVATE_NETWORK_NAME = "private.network.name";
        public static final String PROPERTY_KEY_LOCAL_STORAGE_PATH = "local.storage.path";
        public static final String PROPERTY_KEY_LOCAL_STORAGE_UUID = "local.storage.uuid";
        public static final String PROPERTY_KEY_SCRIPTS_TIMEOUT = "scripts.timeout";
        public static final String PROPERTY_KEY_STOP_SCRIPT_TIMEOUT = "stop.script.timeout";
        public static final String PROPERTY_KEY_CMDS_TIMEOUT = "cmds.timeout";
        public static final String PROPERTY_KEY_VM_MEMBALLOON_DISABLE = "vm.memballoon.disable";
        public static final String PROPERTY_KEY_VM_VIDEO_HARDWARE = "vm.video.hardware";
        public static final String PROPERTY_KEY_VM_VIDEO_RAM = "vm.video.ram";
        public static final String PROPERTY_KEY_HOST_RESERVED_MEM_MB = "host.reserved.mem.mb";
        public static final String PROPERTY_KEY_KVMCLOCK_DISABLE = "kvmclock.disable";
        public static final String PROPERTY_KEY_VM_RNG_ENABLE = "vm.rng.enable";
        public static final String PROPERTY_KEY_VM_RNG_MODEL = "vm.rng.model";
        public static final String PROPERTY_KEY_VM_RNG_PATH = "vm.rng.path";
        public static final String PROPERTY_KEY_VM_WATCHDOG_MODEL = "vm.watchdog.model";
        public static final String PROPERTY_KEY_VM_WATCHDOG_ACTION = "vm.watchdog.action";
        public static final String PROPERTY_KEY_GUEST_CPU_FEATURES = "guest.cpu.features";
        public static final String PROPERTY_KEY_GUEST_CPU_MODEL = "guest.cpu.model";
        public static final String PROPERTY_KEY_GUEST_CPU_MODE = "guest.cpu.mode";
        public static final String PROPERTY_KEY_SYSTEMVM_ISO_PATH = "systemvm.iso.path";
        public static final String PROPERTY_KEY_MOUNT_PATH = "mount.path";
        public static final String PROPERTY_KEY_VM_MIGRATE_DOWNTIME = "vm.migrate.downtime";
        public static final String PROPERTY_KEY_VM_MIGRATE_PAUSEAFTER = "vm.migrate.pauseafter";
        public static final String PROPERTY_KEY_VM_MIGRATE_SPEED = "vm.migrate.speed";

        public static final int DEFAULT_CMDS_TIMEOUT = 7200;
        public static final String DEFAULT_DOMR_SCRIPTS_DIR = "scripts/network/domr";
        public static final long DEFAULT_HOST_RESERVED_MEM_MB = 1047L;
        public static final String DEFAULT_HYPERVISOR_SCRIPTS_DIR = "scripts/vm/hypervisor/kvm";
        public static final String DEFAULT_IPADDR_START = "192.168.166.128";
        public static final HypervisorType DEFAULT_HYPERVISOR_TYPE = HypervisorType.KVM;
        public static final String DEFAULT_LOCAL_STORAGE_PATH = "/var/lib/libvirt/images/";
        public static final String DEFAULT_NETWORK_SCRIPTS_DIR = "scripts/vm/network/vnet";
        public static final String DEFAULT_PRIVATE_MACADDR_START = "00:16:3e:77:e2:a0";
        public static final String DEFAULT_PRIVATE_BRIDGE_NAME = "cloud0";
        public static final String DEFAULT_PUBLIC_NETWORK_DEVICE = "cloudbr0";
        public static final String DEFAULT_PRIVATE_NETWORK_DEVICE = "cloudbr1";
        public static final String DEFAULT_PRIVATE_NETWORK_NAME = "cloud-private";
        public static final int DEFAULT_SCRIPTS_TIMEOUT = 30 * 60;
        public static final int DEFAULT_STOP_SCRIPT_TIMEOUT = 120;
        public static final String DEFAULT_STORAGE_SCRIPTS_DIR = "scripts/storage/qcow2";
        public static final int DEFAULT_VM_VIDEO_RAM = 0;
        public static final String DEFAULT_SYSTEMVM_ISO_PATH = "/opt/cosmic/agent/vms/systemvm.iso";
        public static final String DEFAULT_MOUNT_PATH = "/mnt";
        public static final int DEFAULT_VM_MIGRATE_DOWNTIME = -1;
        public static final int DEFAULT_VM_MIGRATE_PAUSEAFTER = -1;
        public static final int DEFAULT_VM_MIGRATE_SPEED = -1;

        public static final String DEFAULT_VM_RNG_PATH = "/dev/random";
        public static final String SCRIPT_MODIFY_VLAN = "modifyvlan.sh";
        public static final String SCRIPT_VERSIONS = "versions.sh";
        public static final String SCRIPT_SEND_CONFIG_PROPERTIES = "send_config_properties_to_systemvm.py";
        public static final String SCRIPT_KVM_HEART_BEAT = "kvmheartbeat.sh";
        public static final String SCRIPT_CREATE_VM = "createvm.sh";
        public static final String SCRIPT_MANAGE_SNAPSHOT = "managesnapshot.sh";
        public static final String SCRIPT_RESIZE_VOLUME = "resizevolume.sh";
        public static final String SCRIPT_CREATE_TEMPLATE = "createtmplt.sh";
        public static final String SCRIPT_SECURITY_GROUP = "security_group.py";
        public static final String SCRIPT_OVS_TUNNEL = "ovstunnel.py";
        public static final String SCRIPT_ROUTER_PROXY = "router_proxy.sh";
        public static final String SCRIPT_OVS_PVLAN_DHCP_HOST = "ovs-pvlan-dhcp-host.sh";
        public static final String SCRIPT_OVS_PVLAN_VM = "ovs-pvlan-vm.sh";
        public static final String SCRIPT_PING_TEST = "pingtest.sh";
        public static final String SCRIPT_LOCAL_GATEWAY = "ip route |grep default|awk '{print $3}'";
        public static final String SCRIPT_UNAME = "uname -r";

        public static final String FORMAT_NETWORK_SPEED = "ethtool %s |grep Speed | cut -d \\  -f 2";
        public static final String FORMAT_PRIVATE_BRIDGE_NAME = "cloud-%s-0";
        public static final String FORMAT_PRIVATE_NETWORK_NAME = "cloud-%s-private";

        public static final String PATH_PATCH_DIR = "/patch/";
        public static final String PATH_SCRIPTS_NETWORK_DOMR = "scripts/network/domr/";
    }
}
