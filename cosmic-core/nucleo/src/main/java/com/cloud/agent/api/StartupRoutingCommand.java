//

//

package com.cloud.agent.api;

import com.cloud.host.Host;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Networks.RouterPrivateIpStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartupRoutingCommand extends StartupCommand {
    Integer cpuSockets;
    int cpus;
    long speed;
    long memory;
    long dom0MinMemory;
    boolean poolSync;

    String caps;
    String pool;
    HypervisorType hypervisorType;
    Map<String, String> hostDetails; //stuff like host os, cpu capabilities
    List<String> hostTags = new ArrayList<>();
    String hypervisorVersion;
    HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails = new HashMap<>();

    public StartupRoutingCommand() {
        super(Host.Type.Routing);
        hostDetails = new HashMap<>();
        getHostDetails().put(RouterPrivateIpStrategy.class.getCanonicalName(), RouterPrivateIpStrategy.DcGlobal.toString());
    }

    public Map<String, String> getHostDetails() {
        return hostDetails;
    }

    public void setHostDetails(final Map<String, String> hostDetails) {
        this.hostDetails = hostDetails;
    }

    public StartupRoutingCommand(final int cpus, final long speed, final long memory, final long dom0MinMemory, final String caps, final HypervisorType hypervisorType,
                                 final RouterPrivateIpStrategy privIpStrategy) {
        this(cpus, speed, memory, dom0MinMemory, caps, hypervisorType);
        getHostDetails().put(RouterPrivateIpStrategy.class.getCanonicalName(), privIpStrategy.toString());
    }

    public StartupRoutingCommand(final int cpus2, final long speed2, final long memory2, final long dom0MinMemory2, final String caps2, final HypervisorType hypervisorType2) {
        this(cpus2, speed2, memory2, dom0MinMemory2, caps2, hypervisorType2, new HashMap<>());
    }

    public StartupRoutingCommand(final int cpus, final long speed, final long memory, final long dom0MinMemory, final String caps, final HypervisorType hypervisorType,
                                 final Map<String, String> hostDetails) {
        super(Host.Type.Routing);
        this.cpus = cpus;
        this.speed = speed;
        this.memory = memory;
        this.dom0MinMemory = dom0MinMemory;
        this.hypervisorType = hypervisorType;
        this.hostDetails = hostDetails;
        this.caps = caps;
        this.poolSync = false;
    }

    public Integer getCpuSockets() {
        return cpuSockets;
    }

    public void setCpuSockets(final Integer cpuSockets) {
        this.cpuSockets = cpuSockets;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(final int cpus) {
        this.cpus = cpus;
    }

    public String getCapabilities() {
        return caps;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(final long speed) {
        this.speed = speed;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(final long memory) {
        this.memory = memory;
    }

    public long getDom0MinMemory() {
        return dom0MinMemory;
    }

    public void setDom0MinMemory(final long dom0MinMemory) {
        this.dom0MinMemory = dom0MinMemory;
    }

    public void setCaps(final String caps) {
        this.caps = caps;
    }

    public String getPool() {
        return pool;
    }

    public void setPool(final String pool) {
        this.pool = pool;
    }

    public boolean isPoolSync() {
        return poolSync;
    }

    public void setPoolSync(final boolean poolSync) {
        this.poolSync = poolSync;
    }

    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    public void setHypervisorVersion(final String hypervisorVersion) {
        this.hypervisorVersion = hypervisorVersion;
    }

    public List<String> getHostTags() {
        return hostTags;
    }

    public void setHostTags(final String hostTag) {
        this.hostTags.add(hostTag);
    }

    public HashMap<String, HashMap<String, VgpuTypesInfo>> getGpuGroupDetails() {
        return groupDetails;
    }

    public void setGpuGroupDetails(final HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails) {
        this.groupDetails = groupDetails;
    }
}
