package com.cloud.network.security;

import com.cloud.utils.Pair;

import java.util.HashMap;
import java.util.List;

/**
 * Ensures that network firewall rules stay updated as VMs go up and down
 */
public interface SecurityGroupManager {

    public static final String DEFAULT_GROUP_NAME = "default";
    public static final String DEFAULT_GROUP_DESCRIPTION = "Default Security Group";
    public static final int TIME_BETWEEN_CLEANUPS = 60;
    public static final int WORKER_THREAD_COUNT = 10;

    public SecurityGroupVO createSecurityGroup(String name, String description, Long domainId, Long accountId, String accountName);

    public SecurityGroupVO createDefaultSecurityGroup(Long accountId);

    public boolean addInstanceToGroups(Long userVmId, List<Long> groups);

    public void removeInstanceFromGroups(long userVmId);

    public void fullSync(long agentId, HashMap<String, Pair<Long, Long>> newGroupStates);

    public String getSecurityGroupsNamesForVm(long vmId);

    public List<SecurityGroupVO> getSecurityGroupsForVm(long vmId);

    public boolean isVmSecurityGroupEnabled(Long vmId);

    SecurityGroup getDefaultSecurityGroup(long accountId);

    SecurityGroup getSecurityGroup(String name, long accountId);

    boolean isVmMappedToDefaultSecurityGroup(long vmId);
}
