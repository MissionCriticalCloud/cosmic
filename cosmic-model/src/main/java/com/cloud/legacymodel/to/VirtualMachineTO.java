package com.cloud.legacymodel.to;

import com.cloud.legacymodel.vm.BootloaderType;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.OptimiseFor;
import com.cloud.model.enumeration.VirtualMachineType;
import java.util.Date;

import java.util.List;
import java.util.Map;

public class VirtualMachineTO {
    VirtualMachineType type;
    int cpus;
    long minRam;
    long maxRam;
    String hostName;
    String arch;
    String os;
    String cpuflags;
    String manufacturer;
    String platformEmulator;
    String bootArgs;
    String[] bootupScripts;
    boolean enableHA;
    boolean limitCpuUse;
    boolean enableDynamicallyScaleVm;
    String vncPassword;
    Map<String, String> params;
    String uuid;
    DiskTO[] disks;
    NicTO[] nics;
    GPUDeviceTO gpuDevice;
    Integer vcpuMaxLimit;
    List<String[]> vmData = null;
    String configDriveLabel = null;
    String configDriveIsoRootFolder = null;
    String configDriveIsoFile = null;
    MetadataTO metadata;
    private long id;
    private String name;
    private BootloaderType bootloader;
    private String manufacturerString;
    private OptimiseFor optimiseFor;
    private MaintenancePolicy maintenancePolicy;
    private Long bootMenuTimeout;
    private String lastStartDateTime;
    private String lastStartVersion;

    public VirtualMachineTO(final long id, final String instanceName, final VirtualMachineType type, final int cpus, final long minRam, final long maxRam,
                            final BootloaderType bootloader, final String os, final boolean enableHA, final boolean limitCpuUse, final String vncPassword) {
        this.id = id;
        name = instanceName;
        this.type = type;
        this.cpus = cpus;
        this.minRam = minRam;
        this.maxRam = maxRam;
        this.bootloader = bootloader;
        this.os = os;
        this.enableHA = enableHA;
        this.limitCpuUse = limitCpuUse;
        this.vncPassword = vncPassword;
    }

    protected VirtualMachineTO() {
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public boolean isEnableDynamicallyScaleVm() {
        return enableDynamicallyScaleVm;
    }

    public void setEnableDynamicallyScaleVm(final boolean enableDynamicallyScaleVm) {
        this.enableDynamicallyScaleVm = enableDynamicallyScaleVm;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public VirtualMachineType getType() {
        return type;
    }

    public BootloaderType getBootloader() {
        return bootloader;
    }

    public void setBootloader(final BootloaderType bootloader) {
        this.bootloader = bootloader;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(final int cpus) {
        this.cpus = cpus;
    }

    public boolean getLimitCpuUse() {
        return limitCpuUse;
    }

    public long getMinRam() {
        return minRam;
    }

    public void setRam(final long minRam, final long maxRam) {
        this.minRam = minRam;
        this.maxRam = maxRam;
    }

    public long getMaxRam() {
        return maxRam;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(final String arch) {
        this.arch = arch;
    }

    public String getOs() {
        return os;
    }

    public void setOs(final String os) {
        this.os = os;
    }

    public String getBootArgs() {
        return bootArgs;
    }

    public void setBootArgs(final String bootArgs) {
        this.bootArgs = bootArgs;
    }

    public void setBootArgs(final Map<String, String> bootParams) {
        final StringBuilder buf = new StringBuilder();
        for (final Map.Entry<String, String> entry : bootParams.entrySet()) {
            buf.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
        }
        bootArgs = buf.toString();
    }

    public String[] getBootupScripts() {
        return bootupScripts;
    }

    public void setBootupScripts(final String[] bootupScripts) {
        this.bootupScripts = bootupScripts;
    }

    public DiskTO[] getDisks() {
        return disks;
    }

    public void setDisks(final DiskTO[] disks) {
        this.disks = disks;
    }

    public NicTO[] getNics() {
        return nics;
    }

    public void setNics(final NicTO[] nics) {
        this.nics = nics;
    }

    public String getVncPassword() {
        return vncPassword;
    }

    public void setVncPassword(final String vncPassword) {
        this.vncPassword = vncPassword;
    }

    public Map<String, String> getDetails() {
        return params;
    }

    public void setDetails(final Map<String, String> params) {
        this.params = params;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public GPUDeviceTO getGpuDevice() {
        return gpuDevice;
    }

    public void setGpuDevice(final GPUDeviceTO gpuDevice) {
        this.gpuDevice = gpuDevice;
    }

    public String getPlatformEmulator() {
        return platformEmulator;
    }

    public void setPlatformEmulator(final String platformEmulator) {
        this.platformEmulator = platformEmulator;
    }

    public Integer getVcpuMaxLimit() {
        return vcpuMaxLimit;
    }

    public void setVcpuMaxLimit(final Integer vcpuMaxLimit) {
        this.vcpuMaxLimit = vcpuMaxLimit;
    }

    public List<String[]> getVmData() {
        return vmData;
    }

    public void setVmData(final List<String[]> vmData) {
        this.vmData = vmData;
    }

    public String getConfigDriveLabel() {
        return configDriveLabel;
    }

    public void setConfigDriveLabel(final String configDriveLabel) {
        this.configDriveLabel = configDriveLabel;
    }

    public String getConfigDriveIsoRootFolder() {
        return configDriveIsoRootFolder;
    }

    public void setConfigDriveIsoRootFolder(final String configDriveIsoRootFolder) {
        this.configDriveIsoRootFolder = configDriveIsoRootFolder;
    }

    public String getConfigDriveIsoFile() {
        return configDriveIsoFile;
    }

    public void setConfigDriveIsoFile(final String configDriveIsoFile) {
        this.configDriveIsoFile = configDriveIsoFile;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(final String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public MetadataTO getMetadata() {
        return metadata;
    }

    public void setMetadata(final MetadataTO metadata) {
        this.metadata = metadata;
    }

    public String getCpuflags() {
        return cpuflags;
    }

    public void setCpuflags(final String cpuflags) {
        this.cpuflags = cpuflags;
    }

    public String getManufacturerString() {
        return manufacturerString;
    }

    public void setManufacturerString(final String manufacturerString) {
        this.manufacturerString = manufacturerString;
    }

    public OptimiseFor getOptimiseFor() {
        if (optimiseFor == null) {
            return optimiseFor.Generic;
        }
        return optimiseFor;
    }

    public void setOptimiseFor(final OptimiseFor optimiseFor) {
        this.optimiseFor = optimiseFor;
    }

    public MaintenancePolicy getMaintenancePolicy() {
        if (maintenancePolicy == null) {
            return MaintenancePolicy.LiveMigrate;
        }
        return maintenancePolicy;
    }

    public void setMaintenancePolicy(final MaintenancePolicy maintenancePolicy) {
        this.maintenancePolicy = maintenancePolicy;
    }

    public Long getBootMenuTimeout() {
        return bootMenuTimeout;
    }

    public void setBootMenuTimeout(final Long bootMenuTimeout) {
        this.bootMenuTimeout = bootMenuTimeout;
    }

    public String getLastStartDateTime() {
        return lastStartDateTime;
    }

    public void setLastStartDateTime(final String lastStartDateTime) {
        this.lastStartDateTime = lastStartDateTime;
    }

    public String getLastStartVersion() {
        return lastStartVersion;
    }

    public void setLastStartVersion(final String lastStartVersion) {
        this.lastStartVersion = lastStartVersion;
    }
}
