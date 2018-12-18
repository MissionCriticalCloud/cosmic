package com.cloud.agent.resource.kvm;

import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_CMDS_TIMEOUT;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_CPU_SHARES;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_CPU_SHARES_ROUTER;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_DOMR_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_HOST_RESERVED_MEM_MB;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_HYPERVISOR_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_HYPERVISOR_TYPE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_IPADDR_START;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_LOCAL_STORAGE_PATH;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_MOUNT_PATH;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_NETWORK_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_PRIVATE_BRIDGE_NAME;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_PRIVATE_MACADDR_START;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_PRIVATE_NETWORK_DEVICE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_PRIVATE_NETWORK_NAME;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_PUBLIC_NETWORK_DEVICE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_SCRIPTS_TIMEOUT;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_STOP_SCRIPT_TIMEOUT;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_STORAGE_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_SYSTEMVM_ISO_PATH;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_TERMPOLICY_CRASH_SYSTEM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_TERMPOLICY_CRASH_VM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_TERMPOLICY_POWEROFF_SYSTEM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_TERMPOLICY_POWEROFF_VM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_TERMPOLICY_REBOOT_SYSTEM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_TERMPOLICY_REBOOT_VM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_MIGRATE_DOWNTIME;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_MIGRATE_PAUSEAFTER;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_MIGRATE_SPEED;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_MIGRATE_SPEED_ACROSS_CLUSTER;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_RNG_PATH;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.DEFAULT_VM_VIDEO_RAM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.FORMAT_PRIVATE_BRIDGE_NAME;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.FORMAT_PRIVATE_NETWORK_NAME;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_CMDS_TIMEOUT;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_DOMR_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_CPU_FEATURES;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_CPU_MODE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_CPU_MODEL;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_CPU_SHARES;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_CPU_SHARES_ROUTER;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_GUEST_NETWORK_DEVICE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_HOST_RESERVED_MEM_MB;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_HYPERVISOR_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_HYPERVISOR_TYPE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_HYPERVISOR_URI;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_KVMCLOCK_DISABLE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_KVM_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_LOCAL_STORAGE_PATH;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_LOCAL_STORAGE_UUID;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_MOUNT_PATH;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_NETWORK_BRIDGE_TYPE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_NETWORK_DIRECT_DEVICE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_NETWORK_DIRECT_SOURCE_MODE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_NETWORK_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_BRIDGE_NAME;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_IPADDR_START;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_MACADDR_START;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_NETWORK_DEVICE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PRIVATE_NETWORK_NAME;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_PUBLIC_NETWORK_DEVICE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_SCRIPTS_TIMEOUT;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_STOP_SCRIPT_TIMEOUT;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_STORAGE_SCRIPTS_DIR;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_SYSTEMVM_ISO_PATH;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_TERMPOLICY_CRASH_SYSTEM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_TERMPOLICY_CRASH_VM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_TERMPOLICY_POWEROFF_SYSTEM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_TERMPOLICY_POWEROFF_VM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_TERMPOLICY_REBOOT_SYSTEM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_TERMPOLICY_REBOOT_VM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MEMBALLOON_DISABLE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MIGRATE_DOWNTIME;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MIGRATE_PAUSEAFTER;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MIGRATE_SPEED;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_MIGRATE_SPEED_ACROSS_CLUSTER;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_RNG_ENABLE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_RNG_MODEL;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_RNG_PATH;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_VIDEO_HARDWARE;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_VIDEO_RAM;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_WATCHDOG_ACTION;
import static com.cloud.agent.resource.kvm.LibvirtComputingResourceProperties.Constants.PROPERTY_KEY_VM_WATCHDOG_MODEL;
import static com.cloud.utils.CloudConstants.DEFAULT_HOST;
import static com.cloud.utils.CloudConstants.DEFAULT_POD;
import static com.cloud.utils.CloudConstants.DEFAULT_ZONE;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_CLUSTER;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_HOST;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_POD;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_POOL;
import static com.cloud.utils.CloudConstants.PROPERTY_KEY_ZONE;
import static com.cloud.utils.PropertiesUtil.parse;
import static com.cloud.utils.PropertiesUtil.stringSplitDecomposer;

import com.cloud.agent.resource.kvm.LibvirtComputingResource.BridgeType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.RngBackendModel;
import com.cloud.model.enumeration.WatchDogAction;
import com.cloud.model.enumeration.WatchDogModel;
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
    private int guestCpuShares = DEFAULT_CPU_SHARES;
    private int guestCpuSharesRouter = DEFAULT_CPU_SHARES_ROUTER;
    private String guestNetworkDevice;
    private String host = DEFAULT_HOST;
    private long hostReservedMemMb = DEFAULT_HOST_RESERVED_MEM_MB;
    private String hypervisorScriptsDir = DEFAULT_HYPERVISOR_SCRIPTS_DIR;
    private HypervisorType hypervisorType = DEFAULT_HYPERVISOR_TYPE;
    private String hypervisorUri;
    private String instance;
    private String kvmScriptsDir;
    private boolean kvmClockDisable;
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
    private int vmMigrateSpeedAcrossCluster = DEFAULT_VM_MIGRATE_SPEED_ACROSS_CLUSTER;
    private String zone = DEFAULT_ZONE;

    private String termPolicyCrashSystem = DEFAULT_TERMPOLICY_CRASH_SYSTEM;
    private String termPolicyPowerOffSystem = DEFAULT_TERMPOLICY_POWEROFF_SYSTEM;
    private String termPolicyRebootSystem = DEFAULT_TERMPOLICY_REBOOT_SYSTEM;

    private String termPolicyCrashVm = DEFAULT_TERMPOLICY_CRASH_VM;
    private String termPolicyPowerOffVm = DEFAULT_TERMPOLICY_POWEROFF_VM;
    private String termPolicyRebootVm = DEFAULT_TERMPOLICY_REBOOT_VM;

    @Override
    public void load(final Properties properties) {
        this.cluster = parse(properties, PROPERTY_KEY_CLUSTER, this.cluster);
        this.cmdsTimeout = parse(properties, PROPERTY_KEY_CMDS_TIMEOUT, this.cmdsTimeout);
        this.domrScriptsDir = parse(properties, PROPERTY_KEY_DOMR_SCRIPTS_DIR, this.domrScriptsDir);
        this.developer = parse(properties, PROPERTY_KEY_DOMR_SCRIPTS_DIR, this.developer);
        this.guestCpuFeatures = parse(properties, PROPERTY_KEY_GUEST_CPU_FEATURES, this.guestCpuFeatures, stringSplitDecomposer(" ", String.class));
        this.guestCpuModel = parse(properties, PROPERTY_KEY_GUEST_CPU_MODEL, this.guestCpuModel);
        this.guestCpuMode = parse(properties, PROPERTY_KEY_GUEST_CPU_MODE, this.guestCpuMode);
        this.guestCpuShares = parse(properties, PROPERTY_KEY_GUEST_CPU_SHARES, this.guestCpuShares);
        this.guestCpuSharesRouter = parse(properties, PROPERTY_KEY_GUEST_CPU_SHARES_ROUTER, this.guestCpuSharesRouter);
        this.guestNetworkDevice = parse(properties, PROPERTY_KEY_GUEST_NETWORK_DEVICE, this.guestNetworkDevice);
        this.host = parse(properties, PROPERTY_KEY_HOST, this.host);
        this.hostReservedMemMb = parse(properties, PROPERTY_KEY_HOST_RESERVED_MEM_MB, this.hostReservedMemMb);
        this.hypervisorScriptsDir = parse(properties, PROPERTY_KEY_HYPERVISOR_SCRIPTS_DIR, this.hypervisorScriptsDir);
        this.hypervisorUri = parse(properties, PROPERTY_KEY_HYPERVISOR_URI, this.hypervisorUri);
        this.kvmScriptsDir = parse(properties, PROPERTY_KEY_KVM_SCRIPTS_DIR, this.kvmScriptsDir);
        this.kvmClockDisable = parse(properties, PROPERTY_KEY_KVMCLOCK_DISABLE, this.kvmClockDisable);
        this.localStoragePath = parse(properties, PROPERTY_KEY_LOCAL_STORAGE_PATH, this.localStoragePath);
        this.localStorageUuid = parse(properties, PROPERTY_KEY_LOCAL_STORAGE_UUID, this.localStorageUuid);
        this.mountPath = parse(properties, PROPERTY_KEY_MOUNT_PATH, this.mountPath);
        this.networkScriptsDir = parse(properties, PROPERTY_KEY_NETWORK_SCRIPTS_DIR, this.networkScriptsDir);
        this.networkDirectSourceMode = parse(properties, PROPERTY_KEY_NETWORK_DIRECT_SOURCE_MODE, this.networkDirectSourceMode);
        this.networkDirectDevice = parse(properties, PROPERTY_KEY_NETWORK_DIRECT_DEVICE, this.networkDirectDevice);
        this.pool = parse(properties, PROPERTY_KEY_POOL, this.pool);
        this.pod = parse(properties, PROPERTY_KEY_POD, this.pod);
        this.privateMacaddrStart = parse(properties, PROPERTY_KEY_PRIVATE_MACADDR_START, this.privateMacaddrStart);
        this.privateIpaddrStart = parse(properties, PROPERTY_KEY_PRIVATE_IPADDR_START, this.privateIpaddrStart);
        this.privateBridgeName = parse(properties, PROPERTY_KEY_PRIVATE_BRIDGE_NAME, this.privateBridgeName);
        this.publicNetworkDevice = parse(properties, PROPERTY_KEY_PUBLIC_NETWORK_DEVICE, this.publicNetworkDevice);
        this.privateNetworkDevice = parse(properties, PROPERTY_KEY_PRIVATE_NETWORK_DEVICE, this.privateNetworkDevice);
        this.privateNetworkName = parse(properties, PROPERTY_KEY_PRIVATE_NETWORK_NAME, this.privateNetworkName);
        this.storageScriptsDir = parse(properties, PROPERTY_KEY_STORAGE_SCRIPTS_DIR, this.storageScriptsDir);
        this.scriptsTimeout = parse(properties, PROPERTY_KEY_SCRIPTS_TIMEOUT, this.scriptsTimeout);
        this.stopScriptTimeout = parse(properties, PROPERTY_KEY_STOP_SCRIPT_TIMEOUT, this.stopScriptTimeout);
        this.systemvmIsoPath = parse(properties, PROPERTY_KEY_SYSTEMVM_ISO_PATH, this.systemvmIsoPath);
        this.vmMemballoonDisable = parse(properties, PROPERTY_KEY_VM_MEMBALLOON_DISABLE, this.vmMemballoonDisable);
        this.vmVideoHardware = parse(properties, PROPERTY_KEY_VM_VIDEO_HARDWARE, this.vmVideoHardware);
        this.vmVideoRam = parse(properties, PROPERTY_KEY_VM_VIDEO_RAM, this.vmVideoRam);
        this.vmRngEnable = parse(properties, PROPERTY_KEY_VM_RNG_ENABLE, this.vmRngEnable);
        this.vmRngPath = parse(properties, PROPERTY_KEY_VM_RNG_PATH, this.vmRngPath);
        this.vmMigrateDowntime = parse(properties, PROPERTY_KEY_VM_MIGRATE_DOWNTIME, this.vmMigrateDowntime);
        this.vmMigratePauseafter = parse(properties, PROPERTY_KEY_VM_MIGRATE_PAUSEAFTER, this.vmMigratePauseafter);
        this.vmMigrateSpeed = parse(properties, PROPERTY_KEY_VM_MIGRATE_SPEED, this.vmMigrateSpeed);
        this.vmMigrateSpeedAcrossCluster = parse(properties, PROPERTY_KEY_VM_MIGRATE_SPEED_ACROSS_CLUSTER, this.vmMigrateSpeedAcrossCluster);
        this.zone = parse(properties, PROPERTY_KEY_ZONE, this.zone);

        this.hypervisorType = parse(properties, PROPERTY_KEY_HYPERVISOR_TYPE, this.hypervisorType, HypervisorType.class);
        this.networkBridgeType = parse(properties, PROPERTY_KEY_NETWORK_BRIDGE_TYPE, this.networkBridgeType, BridgeType.class);
        this.vmRngModel = parse(properties, PROPERTY_KEY_VM_RNG_MODEL, this.vmRngModel, RngBackendModel.class);
        this.vmWatchdogModel = parse(properties, PROPERTY_KEY_VM_WATCHDOG_MODEL, this.vmWatchdogModel, WatchDogModel.class);
        this.vmWatchdogAction = parse(properties, PROPERTY_KEY_VM_WATCHDOG_ACTION, this.vmWatchdogAction, WatchDogAction.class);

        this.termPolicyCrashSystem = parse(properties, PROPERTY_KEY_TERMPOLICY_CRASH_SYSTEM, this.termPolicyCrashSystem);
        this.termPolicyPowerOffSystem = parse(properties, PROPERTY_KEY_TERMPOLICY_POWEROFF_SYSTEM, this.termPolicyPowerOffSystem);
        this.termPolicyRebootSystem = parse(properties, PROPERTY_KEY_TERMPOLICY_REBOOT_SYSTEM, this.termPolicyRebootSystem);

        this.termPolicyCrashVm = parse(properties, PROPERTY_KEY_TERMPOLICY_CRASH_VM, this.termPolicyCrashVm);
        this.termPolicyPowerOffVm = parse(properties, PROPERTY_KEY_TERMPOLICY_POWEROFF_VM, this.termPolicyPowerOffVm);
        this.termPolicyRebootVm = parse(properties, PROPERTY_KEY_TERMPOLICY_REBOOT_VM, this.termPolicyRebootVm);
        validateValues();
    }

    private void validateValues() {
        this.hypervisorType = this.hypervisorType == HypervisorType.None ? HypervisorType.KVM : this.hypervisorType;
        this.hypervisorUri = this.hypervisorUri == null ? LibvirtConnection.getHypervisorUri(this.hypervisorType.toString()) : this.hypervisorUri;
        this.privateBridgeName = this.developer ? String.format(FORMAT_PRIVATE_BRIDGE_NAME, this.instance) : this.privateBridgeName;
        this.guestNetworkDevice = this.guestNetworkDevice == null ? this.privateNetworkDevice : this.guestNetworkDevice;
        this.privateNetworkName = this.developer ? String.format(FORMAT_PRIVATE_NETWORK_NAME, this.instance) : this.privateNetworkName;
        this.localStoragePath = new File(this.localStoragePath).getAbsolutePath();
        this.localStorageUuid = this.localStorageUuid == null ? UUID.randomUUID().toString() : this.localStorageUuid;
    }

    @Override
    public Map<String, Object> buildPropertiesMap() {
        final HashMap<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put(PROPERTY_KEY_CLUSTER, this.cluster);
        propertiesMap.put(PROPERTY_KEY_CMDS_TIMEOUT, this.cmdsTimeout);
        propertiesMap.put(PROPERTY_KEY_DOMR_SCRIPTS_DIR, this.domrScriptsDir);
        propertiesMap.put(PROPERTY_KEY_GUEST_CPU_FEATURES, this.guestCpuFeatures);
        propertiesMap.put(PROPERTY_KEY_GUEST_CPU_MODEL, this.guestCpuModel);
        propertiesMap.put(PROPERTY_KEY_GUEST_CPU_MODE, this.guestCpuMode);
        propertiesMap.put(PROPERTY_KEY_GUEST_CPU_SHARES, this.guestCpuShares);
        propertiesMap.put(PROPERTY_KEY_GUEST_CPU_SHARES_ROUTER, this.guestCpuSharesRouter);
        propertiesMap.put(PROPERTY_KEY_GUEST_NETWORK_DEVICE, this.guestNetworkDevice);
        propertiesMap.put(PROPERTY_KEY_HOST, this.host);
        propertiesMap.put(PROPERTY_KEY_HOST_RESERVED_MEM_MB, this.hostReservedMemMb);
        propertiesMap.put(PROPERTY_KEY_HYPERVISOR_SCRIPTS_DIR, this.hypervisorScriptsDir);
        propertiesMap.put(PROPERTY_KEY_HYPERVISOR_TYPE, this.hypervisorType);
        propertiesMap.put(PROPERTY_KEY_HYPERVISOR_URI, this.hypervisorUri);
        propertiesMap.put(PROPERTY_KEY_KVM_SCRIPTS_DIR, this.kvmScriptsDir);
        propertiesMap.put(PROPERTY_KEY_KVMCLOCK_DISABLE, this.kvmClockDisable);
        propertiesMap.put(PROPERTY_KEY_LOCAL_STORAGE_PATH, this.localStoragePath);
        propertiesMap.put(PROPERTY_KEY_LOCAL_STORAGE_UUID, this.localStorageUuid);
        propertiesMap.put(PROPERTY_KEY_MOUNT_PATH, this.mountPath);
        propertiesMap.put(PROPERTY_KEY_NETWORK_SCRIPTS_DIR, this.networkScriptsDir);
        propertiesMap.put(PROPERTY_KEY_NETWORK_BRIDGE_TYPE, this.networkBridgeType);
        propertiesMap.put(PROPERTY_KEY_NETWORK_DIRECT_SOURCE_MODE, this.networkDirectSourceMode);
        propertiesMap.put(PROPERTY_KEY_NETWORK_DIRECT_DEVICE, this.networkDirectDevice);
        propertiesMap.put(PROPERTY_KEY_POOL, this.pool);
        propertiesMap.put(PROPERTY_KEY_POD, this.pod);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_MACADDR_START, this.privateMacaddrStart);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_IPADDR_START, this.privateIpaddrStart);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_BRIDGE_NAME, this.privateBridgeName);
        propertiesMap.put(PROPERTY_KEY_PUBLIC_NETWORK_DEVICE, this.publicNetworkDevice);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_NETWORK_DEVICE, this.privateNetworkDevice);
        propertiesMap.put(PROPERTY_KEY_PRIVATE_NETWORK_NAME, this.privateNetworkName);
        propertiesMap.put(PROPERTY_KEY_STORAGE_SCRIPTS_DIR, this.storageScriptsDir);
        propertiesMap.put(PROPERTY_KEY_SCRIPTS_TIMEOUT, this.scriptsTimeout);
        propertiesMap.put(PROPERTY_KEY_STOP_SCRIPT_TIMEOUT, this.stopScriptTimeout);
        propertiesMap.put(PROPERTY_KEY_SYSTEMVM_ISO_PATH, this.systemvmIsoPath);
        propertiesMap.put(PROPERTY_KEY_VM_MEMBALLOON_DISABLE, this.vmMemballoonDisable);
        propertiesMap.put(PROPERTY_KEY_VM_VIDEO_HARDWARE, this.vmVideoHardware);
        propertiesMap.put(PROPERTY_KEY_VM_VIDEO_RAM, this.vmVideoRam);
        propertiesMap.put(PROPERTY_KEY_VM_RNG_ENABLE, this.vmRngEnable);
        propertiesMap.put(PROPERTY_KEY_VM_RNG_MODEL, this.vmRngModel);
        propertiesMap.put(PROPERTY_KEY_VM_RNG_PATH, this.vmRngPath);
        propertiesMap.put(PROPERTY_KEY_VM_WATCHDOG_MODEL, this.vmWatchdogModel);
        propertiesMap.put(PROPERTY_KEY_VM_WATCHDOG_ACTION, this.vmWatchdogAction);
        propertiesMap.put(PROPERTY_KEY_VM_MIGRATE_DOWNTIME, this.vmMigrateDowntime);
        propertiesMap.put(PROPERTY_KEY_VM_MIGRATE_PAUSEAFTER, this.vmMigratePauseafter);
        propertiesMap.put(PROPERTY_KEY_VM_MIGRATE_SPEED, this.vmMigrateSpeed);
        propertiesMap.put(PROPERTY_KEY_VM_MIGRATE_SPEED_ACROSS_CLUSTER, this.vmMigrateSpeedAcrossCluster);
        propertiesMap.put(PROPERTY_KEY_ZONE, this.zone);
        return propertiesMap;
    }

    public String getCluster() {
        return this.cluster;
    }

    public int getCmdsTimeout() {
        return this.cmdsTimeout * 1000;
    }

    public String getGuestCpuModel() {
        return this.guestCpuModel;
    }

    public String getGuestCpuMode() {
        return this.guestCpuMode;
    }

    public int getGuestCpuShares() {
        return this.guestCpuShares;
    }

    public int getGuestCpuSharesRouter() {
        return this.guestCpuSharesRouter;
    }

    public String getGuestNetworkDevice() {
        return this.guestNetworkDevice;
    }

    public String getHost() {
        return this.host;
    }

    public long getHostReservedMemMb() {
        return this.hostReservedMemMb * 1024 * 1024L;
    }

    public String getHypervisorScriptsDir() {
        return this.hypervisorScriptsDir;
    }

    public HypervisorType getHypervisorType() {
        return this.hypervisorType;
    }

    public String getHypervisorUri() {
        return this.hypervisorUri;
    }

    public boolean isKvmClockDisable() {
        return this.kvmClockDisable;
    }

    public String getLocalStoragePath() {
        return this.localStoragePath;
    }

    public String getLocalStorageUuid() {
        return this.localStorageUuid;
    }

    public String getNetworkScriptsDir() {
        return this.networkScriptsDir;
    }

    public BridgeType getNetworkBridgeType() {
        return this.networkBridgeType;
    }

    public String getPod() {
        return this.pod;
    }

    public String getPrivateBridgeName() {
        return this.privateBridgeName;
    }

    public String getPublicNetworkDevice() {
        return this.publicNetworkDevice;
    }

    public String getPrivateNetworkDevice() {
        return this.privateNetworkDevice;
    }

    public String getStorageScriptsDir() {
        return this.storageScriptsDir;
    }

    public int getScriptsTimeout() {
        return this.scriptsTimeout * 1000;
    }

    public int getStopScriptTimeout() {
        return this.stopScriptTimeout * 1000;
    }

    public String getSystemvmIsoPath() {
        return this.systemvmIsoPath;
    }

    public boolean getVmMemballoonDisable() {
        return this.vmMemballoonDisable;
    }

    public String getVmVideoHardware() {
        return this.vmVideoHardware;
    }

    public int getVmVideoRam() {
        return this.vmVideoRam;
    }

    public boolean getVmRngEnable() {
        return this.vmRngEnable;
    }

    public int getVmMigrateDowntime() {
        return this.vmMigrateDowntime;
    }

    public int getVmMigratePauseafter() {
        return this.vmMigratePauseafter;
    }

    public int getVmMigrateSpeed() {
        return this.vmMigrateSpeed;
    }

    public int getVmMigrateSpeedAcrossCluster() {
        return vmMigrateSpeedAcrossCluster;
    }

    public List<String> getGuestCpuFeatures() {
        return this.guestCpuFeatures;
    }

    public String getSystemTermPolicyCrash() {
        return this.termPolicyCrashSystem;
    }

    public String getSystemTermPolicyPowerOff() {
        return this.termPolicyPowerOffSystem;
    }

    public String getSystemTermPolicyReboot() {
        return this.termPolicyRebootSystem;
    }

    public String getVmTermPolicyCrash() {
        return this.termPolicyCrashVm;
    }

    public String getVmTermPolicyPowerOff() {
        return this.termPolicyPowerOffVm;
    }

    public String getVmTermPolicyReboot() {
        return this.termPolicyRebootVm;
    }

    public String getInstance() {
        return this.instance;
    }

    public String getPool() {
        return this.pool;
    }

    public String getZone() {
        return this.zone;
    }

    public boolean isDeveloper() {
        return this.developer;
    }

    public boolean hasGuestCpuMode() {
        return this.guestCpuMode != null;
    }

    public void unsetGuestCpuMode() {
        this.guestCpuMode = "";
    }

    public void unsetGuestCpuModel() {
        this.guestCpuModel = "";
    }

    public void setVmMigrateSpeed(final int vmMigrateSpeed) {
        this.vmMigrateSpeed = vmMigrateSpeed;
    }

    public void setNetworkBridgeType(final BridgeType bridgeType) {
        this.networkBridgeType = bridgeType;
    }

    public WatchDogAction getVmWatchdogAction() {
        return this.vmWatchdogAction;
    }

    public WatchDogModel getVmWatchdogModel() {
        return this.vmWatchdogModel;
    }

    public static class Constants {
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
        public static final String PROPERTY_KEY_GUEST_CPU_SHARES = "guest.cpu.shares";
        public static final String PROPERTY_KEY_GUEST_CPU_SHARES_ROUTER = "guest.cpu.shares.router";
        public static final String PROPERTY_KEY_SYSTEMVM_ISO_PATH = "systemvm.iso.path";
        public static final String PROPERTY_KEY_MOUNT_PATH = "mount.path";
        public static final String PROPERTY_KEY_VM_MIGRATE_DOWNTIME = "vm.migrate.downtime";
        public static final String PROPERTY_KEY_VM_MIGRATE_PAUSEAFTER = "vm.migrate.pauseafter";
        public static final String PROPERTY_KEY_VM_MIGRATE_SPEED = "vm.migrate.speed";
        public static final String PROPERTY_KEY_VM_MIGRATE_SPEED_ACROSS_CLUSTER = "vm.migrate.speedacrosscluster";

        public static final String PROPERTY_KEY_TERMPOLICY_CRASH_SYSTEM = "termpolicy.system.oncrash";
        public static final String PROPERTY_KEY_TERMPOLICY_POWEROFF_SYSTEM = "termpolicy.system.onpoweroff";
        public static final String PROPERTY_KEY_TERMPOLICY_REBOOT_SYSTEM = "termpolicy.system.onreboot";

        public static final String PROPERTY_KEY_TERMPOLICY_CRASH_VM = "termpolicy.vm.oncrash";
        public static final String PROPERTY_KEY_TERMPOLICY_POWEROFF_VM = "termpolicy.vm.onpoweroff";
        public static final String PROPERTY_KEY_TERMPOLICY_REBOOT_VM = "termpolicy.vm.onreboot";

        public static final int DEFAULT_CPU_SHARES = 1024;
        public static final int DEFAULT_CPU_SHARES_ROUTER = 1024;
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
        public static final int DEFAULT_VM_MIGRATE_SPEED_ACROSS_CLUSTER = 0;

        public static final String DEFAULT_TERMPOLICY_CRASH_SYSTEM = "restart";
        public static final String DEFAULT_TERMPOLICY_POWEROFF_SYSTEM = "destroy";
        public static final String DEFAULT_TERMPOLICY_REBOOT_SYSTEM = "restart";

        public static final String DEFAULT_TERMPOLICY_CRASH_VM = "destroy";
        public static final String DEFAULT_TERMPOLICY_POWEROFF_VM = "destroy";
        public static final String DEFAULT_TERMPOLICY_REBOOT_VM = "destroy";

        public static final String DEFAULT_VM_RNG_PATH = "/dev/random";
        public static final String SCRIPT_MODIFY_VLAN = "modifyvlan.sh";
        public static final String SCRIPT_MODIFY_VXLAN = "modifyvxlan.sh";
        public static final String SCRIPT_VERSIONS = "versions.sh";
        public static final String SCRIPT_SEND_CONFIG_PROPERTIES = "send_config_properties_to_systemvm.py";
        public static final String SCRIPT_KVM_HEART_BEAT = "kvmheartbeat.sh";
        public static final String SCRIPT_MANAGE_SNAPSHOT = "managesnapshot.sh";
        public static final String SCRIPT_CREATE_TEMPLATE = "createtmplt.sh";
        public static final String SCRIPT_ROUTER_PROXY = "router_proxy.sh";
        public static final String SCRIPT_OVS_PVLAN_DHCP_HOST = "ovs-pvlan-dhcp-host.sh";
        public static final String SCRIPT_OVS_PVLAN_VM = "ovs-pvlan-vm.sh";
        public static final String SCRIPT_PING_TEST = "pingtest.sh";
        public static final String SCRIPT_LOCAL_GATEWAY = "ip route |grep default|awk '{print $3}'";

        public static final String FORMAT_NETWORK_SPEED = "ethtool %s |grep Speed | cut -d \\  -f 2";
        public static final String FORMAT_PRIVATE_BRIDGE_NAME = "cloud-%s-0";
        public static final String FORMAT_PRIVATE_NETWORK_NAME = "cloud-%s-private";

        public static final String PATH_PATCH_DIR = "/patch/";
        public static final String PATH_SCRIPTS_NETWORK_DOMR = "scripts/network/domr/";
    }
}
