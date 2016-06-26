package com.cloud.deploy;

import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.framework.config.ConfigKey;

import java.util.List;

/**
 */
public interface DeploymentClusterPlanner extends DeploymentPlanner {

    static final String ClusterCPUCapacityDisableThresholdCK = "cluster.cpu.allocated.capacity.disablethreshold";
    static final String ClusterMemoryCapacityDisableThresholdCK = "cluster.memory.allocated.capacity.disablethreshold";

    static final ConfigKey<Float> ClusterCPUCapacityDisableThreshold =
            new ConfigKey<>(
                    Float.class,
                    ClusterCPUCapacityDisableThresholdCK,
                    "Alert",
                    "0.85",
                    "Percentage (as a value between 0 and 1) of cpu utilization above which allocators will disable using the cluster for low cpu available. Keep the " +
                            "corresponding notification threshold lower than this to be notified beforehand.",
                    true, ConfigKey.Scope.Cluster, null);
    static final ConfigKey<Float> ClusterMemoryCapacityDisableThreshold =
            new ConfigKey<>(
                    Float.class,
                    ClusterMemoryCapacityDisableThresholdCK,
                    "Alert",
                    "0.85",
                    "Percentage (as a value between 0 and 1) of memory utilization above which allocators will disable using the cluster for low memory available. Keep the " +
                            "corresponding notification threshold lower than this to be notified beforehand.",
                    true, ConfigKey.Scope.Cluster, null);

    /**
     * This is called to determine list of possible clusters where a virtual
     * machine can be deployed.
     *
     * @param vm    virtual machine.
     * @param plan  deployment plan that tells you where it's being deployed to.
     * @param avoid avoid these data centers, pods, clusters, or hosts.
     * @return DeployDestination for that virtual machine.
     */
    List<Long> orderClusters(VirtualMachineProfile vm, DeploymentPlan plan, ExcludeList avoid) throws InsufficientServerCapacityException;

    PlannerResourceUsage getResourceUsage(VirtualMachineProfile vmProfile, DeploymentPlan plan, ExcludeList avoid) throws InsufficientServerCapacityException;
}
