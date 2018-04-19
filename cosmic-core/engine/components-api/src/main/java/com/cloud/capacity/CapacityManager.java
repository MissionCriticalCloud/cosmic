package com.cloud.capacity;

import com.cloud.framework.config.ConfigKey;
import com.cloud.host.Host;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.vm.VirtualMachine;

/**
 * Capacity Manager manages the different capacities
 * available within the Cloud Stack.
 */
public interface CapacityManager {

    static final String CpuOverprovisioningFactorCK = "cpu.overprovisioning.factor";
    static final String MemOverprovisioningFactorCK = "mem.overprovisioning.factor";
    static final String StorageCapacityDisableThresholdCK = "pool.storage.capacity.disablethreshold";
    static final String StorageOverprovisioningFactorCK = "storage.overprovisioning.factor";
    static final String StorageIopsOverprovisioningFactorCK = "storage.iopsoverprovisioning.factor";
    static final String StorageAllocatedCapacityDisableThresholdCK = "pool.storage.allocated.capacity.disablethreshold";

    static final ConfigKey<Float> CpuOverprovisioningFactor = new ConfigKey<>(Float.class, CpuOverprovisioningFactorCK, "Advanced", "1.0",
            "Used for CPU overprovisioning calculation; available CPU will be (actualCpuCapacity * cpu.overprovisioning.factor)", true, ConfigKey.Scope.Cluster, null);
    static final ConfigKey<Float> MemOverprovisioningFactor = new ConfigKey<>(Float.class, MemOverprovisioningFactorCK, "Advanced", "1.0",
            "Used for memory overprovisioning calculation", true, ConfigKey.Scope.Cluster, null);
    static final ConfigKey<Double> StorageCapacityDisableThreshold = new ConfigKey<>("Alert", Double.class, StorageCapacityDisableThresholdCK, "0.85",
            "Percentage (as a value between 0 and 1) of storage utilization above which allocators will disable using the pool for low storage available.", true,
            ConfigKey.Scope.Zone);
    static final ConfigKey<Double> StorageOverprovisioningFactor = new ConfigKey<>("Storage", Double.class, StorageOverprovisioningFactorCK, "2",
            "Used for storage overprovisioning calculation; available storage will be (actualStorageSize * storage.overprovisioning.factor)", true, ConfigKey.Scope.StoragePool);
    static final ConfigKey<Double> StorageIopsOverprovisioningFactor = new ConfigKey<>("Storage", Double.class, StorageIopsOverprovisioningFactorCK, "2",
            "Used for storage IOPS overprovisioning calculation; available IOPS will be (actual IOPS * storage.iopsoverprovisioning.factor)", true, ConfigKey.Scope.StoragePool);
    static final ConfigKey<Double> StorageAllocatedCapacityDisableThreshold =
            new ConfigKey<>(
                    "Alert",
                    Double.class,
                    StorageAllocatedCapacityDisableThresholdCK,
                    "0.85",
                    "Percentage (as a value between 0 and 1) of allocated storage utilization above which allocators will disable using the pool for low allocated storage " +
                            "available.",
                    true, ConfigKey.Scope.Zone);

    public boolean releaseVmCapacity(VirtualMachine vm, boolean moveFromReserved, boolean moveToReservered, Long hostId);

    void allocateVmCapacity(VirtualMachine vm, boolean fromLastHost);

    /**
     * @param hostId                    Id of the host to check capacity
     * @param cpu                       required CPU
     * @param ram                       required RAM
     * @param cpuOverprovisioningFactor factor to apply to the actual host cpu
     */
    boolean checkIfHostHasCapacity(long hostId, Integer cpu, long ram, boolean checkFromReservedCapacity, float cpuOverprovisioningFactor, float memoryOvercommitRatio,
                                   boolean considerReservedCapacity);

    void updateCapacityForHost(Host host);

    /**
     * @param pool                  storage pool
     * @param templateForVmCreation template that will be used for vm creation
     * @return total allocated capacity for the storage pool
     */
    long getAllocatedPoolCapacity(StoragePoolVO pool, VMTemplateVO templateForVmCreation);

    /**
     * Check if specified host's running VM count has reach hypervisor limit
     *
     * @param host the host to be checked
     * @return true if the count of host's running VMs >= hypervisor limit
     */
    boolean checkIfHostReachMaxGuestLimit(Host host);

    /**
     * Check if specified host has capability to support cpu cores
     *
     * @param hostId the host to be checked
     * @param cpuNum cpu number to check
     * @return true if the count of host's running VMs >= hypervisor limit
     */
    boolean checkIfHostHasCpuCapability(long hostId, Integer cpuNum);

    /**
     * Check if cluster will cross threshold if the cpu/memory requested are accomodated
     *
     * @param clusterId    the clusterId to check
     * @param cpuRequested cpu requested
     * @param ramRequested cpu requested
     * @return true if the customer crosses threshold, false otherwise
     */
    boolean checkIfClusterCrossesThreshold(Long clusterId, Integer cpuRequested, long ramRequested);

    float getClusterOverProvisioningFactor(Long clusterId, short capacityType);

    long getUsedBytes(StoragePoolVO pool);

    long getUsedIops(StoragePoolVO pool);
}
