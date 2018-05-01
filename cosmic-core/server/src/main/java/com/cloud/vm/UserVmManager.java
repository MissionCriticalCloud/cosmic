package com.cloud.vm;

import com.cloud.agent.api.VmDiskStatsEntry;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.api.BaseCmd.HTTPMethod;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ManagementServerException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.VirtualMachineMigrationException;
import com.cloud.framework.config.ConfigKey;
import com.cloud.legacymodel.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public interface UserVmManager extends UserVmService {
    String EnableDynamicallyScaleVmCK = "enable.dynamic.scale.vm";
    String AllowUserExpungeRecoverVmCK = "allow.user.expunge.recover.vm";
    ConfigKey<Boolean> EnableDynamicallyScaleVm = new ConfigKey<>("Advanced", Boolean.class, EnableDynamicallyScaleVmCK, "false",
            "Enables/Disables dynamically scaling a vm", true, ConfigKey.Scope.Zone);
    ConfigKey<Boolean> AllowUserExpungeRecoverVm = new ConfigKey<>("Advanced", Boolean.class, AllowUserExpungeRecoverVmCK, "false",
            "Determines whether users can expunge or recover their vm", true, ConfigKey.Scope.Account);

    int MAX_USER_DATA_LENGTH_BYTES = 2048;

    /**
     * @param vmId id of the virtual machine.
     * @return VirtualMachine
     */
    UserVmVO getVirtualMachine(long vmId);

    /**
     * Stops the virtual machine
     *
     * @param userId the id of the user performing the action
     * @param vmId
     * @return true if stopped; false if problems.
     */
    boolean stopVirtualMachine(long userId, long vmId);

    /**
     * Obtains statistics for a list of host or VMs; CPU and network utilization
     *
     * @return GetVmStatsAnswer
     */
    HashMap<Long, VmStatsEntry> getVirtualMachineStatistics(long hostId, String hostName, List<Long> vmIds);

    HashMap<Long, List<VmDiskStatsEntry>> getVmDiskStatistics(long hostId, String hostName, List<Long> vmIds);

    boolean deleteVmGroup(long groupId);

    boolean addInstanceToGroup(long userVmId, String group);

    InstanceGroupVO getGroupForVm(long vmId);

    void removeInstanceFromInstanceGroup(long vmId);

    boolean expunge(UserVmVO vm, long callerUserId, Account caller);

    Pair<UserVmVO, Map<VirtualMachineProfile.Param, Object>> startVirtualMachine(long vmId, Long hostId, Map<VirtualMachineProfile.Param, Object> additionalParams, String
            deploymentPlannerToUse)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    boolean upgradeVirtualMachine(Long id, Long serviceOfferingId, Map<String, String> customParameters) throws ResourceUnavailableException,
            ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException;

    boolean setupVmForPvlan(boolean add, Long hostId, NicProfile nic);

    void collectVmDiskStatistics(UserVmVO userVm);

    UserVm updateVirtualMachine(long id, String displayName, String group, Boolean ha, Boolean isDisplayVmEnabled, Long osTypeId, String userData,
                                Boolean isDynamicallyScalable, HTTPMethod httpMethod, String customId, String hostName, String instanceName) throws ResourceUnavailableException,
            InsufficientCapacityException;
}
