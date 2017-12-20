package com.cloud.engine.service.api;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.engine.cloud.entity.api.VirtualMachineEntity;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.vm.NicProfile;

import java.util.List;
import java.util.Map;

public interface OrchestrationService {

    void createVirtualMachine(String id, String owner, String templateId, String hostName, String displayName, String hypervisor, int cpu, int speed, long memory, Long diskSize,
                              List<String> computeTags, List<String> rootDiskTags, Map<String, NicProfile> networkNicMap, DeploymentPlan plan, Long rootDiskSize) throws
            InsufficientCapacityException;

    void createVirtualMachineFromScratch(String id, String owner, String isoId, String hostName, String displayName, String hypervisor, String os, int cpu, int speed, long memory,
                                         Long diskSize, List<String> computeTags, List<String> rootDiskTags, Map<String, NicProfile> networkNicMap, DeploymentPlan plan) throws
            InsufficientCapacityException;

    VirtualMachineEntity getVirtualMachine(String id);
}
