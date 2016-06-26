package com.cloud.agent.api.to;

import com.cloud.template.VirtualMachineTemplate.BootloaderType;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Type;

import java.util.List;
import java.util.Map;

public class VirtualMachineTO {
    Type type;
    int cpus;
    /**
     * 'speed' is still here since 4.0.X/4.1.X management servers do not support
     * the overcommit feature yet.
     * <p>
     * The overcommit feature sends minSpeed and maxSpeed
     * <p>
     * So this is here for backwards compatibility with 4.0.X/4.1.X management servers
     * and newer agents.
     */
    Integer speed;
    Integer minSpeed;
    Integer maxSpeed;
    long minRam;
    long maxRam;
    String hostName;
    String arch;
    String os;
    String platformEmulator;
    String bootArgs;
    String[] bootupScripts;
    boolean enableHA;
    boolean limitCpuUse;
    boolean enableDynamicallyScaleVm;
    String vncPassword;
    String vncAddr;
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
    private long id;
    private String name;
    private BootloaderType bootloader;

    public VirtualMachineTO(final long id, final String instanceName, final VirtualMachine.Type type, final int cpus, final Integer speed, final long minRam, final long maxRam,
                            final BootloaderType bootloader,
                            final String os, final boolean enableHA, final boolean limitCpuUse, final String vncPassword) {
        this.id = id;
        name = instanceName;
        this.type = type;
        this.cpus = cpus;
        this.speed = speed;
        this.minRam = minRam;
        this.maxRam = maxRam;
        this.bootloader = bootloader;
        this.os = os;
        this.enableHA = enableHA;
        this.limitCpuUse = limitCpuUse;
        this.vncPassword = vncPassword;
    }

    public VirtualMachineTO(final long id, final String instanceName, final VirtualMachine.Type type, final int cpus, final Integer minSpeed, final Integer maxSpeed, final long
            minRam, final long maxRam,
                            final BootloaderType bootloader, final String os, final boolean enableHA, final boolean limitCpuUse, final String vncPassword) {
        this.id = id;
        name = instanceName;
        this.type = type;
        this.cpus = cpus;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
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

    public Type getType() {
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

    public Integer getSpeed() {
        return speed;
    }

    public Integer getMinSpeed() {
        return minSpeed;
    }

    public Integer getMaxSpeed() {
        return maxSpeed;
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

    public String getVncAddr() {
        return vncAddr;
    }

    public void setVncAddr(final String vncAddr) {
        this.vncAddr = vncAddr;
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
}
