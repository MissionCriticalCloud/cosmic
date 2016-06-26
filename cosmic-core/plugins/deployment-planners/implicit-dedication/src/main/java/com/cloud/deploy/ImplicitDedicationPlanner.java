package com.cloud.deploy;

import com.cloud.configuration.Config;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.host.HostVO;
import com.cloud.resource.ResourceManager;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.service.dao.ServiceOfferingDetailsDao;
import com.cloud.user.Account;
import com.cloud.utils.DateUtil;
import com.cloud.utils.NumbersUtil;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineProfile;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImplicitDedicationPlanner extends FirstFitPlanner implements DeploymentClusterPlanner {

    private static final Logger s_logger = LoggerFactory.getLogger(ImplicitDedicationPlanner.class);

    @Inject
    private ServiceOfferingDao serviceOfferingDao;
    @Inject
    private ServiceOfferingDetailsDao serviceOfferingDetailsDao;
    @Inject
    private ResourceManager resourceMgr;

    private int capacityReleaseInterval;

    @Override
    public List<Long> orderClusters(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid) throws InsufficientServerCapacityException {
        List<Long> clusterList = super.orderClusters(vmProfile, plan, avoid);
        final Set<Long> hostsToAvoid = avoid.getHostsToAvoid();
        final Account account = vmProfile.getOwner();

        if (clusterList == null || clusterList.isEmpty()) {
            return clusterList;
        }

        // Check if strict or preferred mode should be used.
        final boolean preferred = isServiceOfferingUsingPlannerInPreferredMode(vmProfile.getServiceOfferingId());

        // Get the list of all the hosts in the given clusters
        final List<Long> allHosts = new ArrayList<>();
        for (final Long cluster : clusterList) {
            final List<HostVO> hostsInCluster = resourceMgr.listAllHostsInCluster(cluster);
            for (final HostVO hostVO : hostsInCluster) {
                allHosts.add(hostVO.getId());
            }
        }

        // Go over all the hosts in the cluster and get a list of
        // 1. All empty hosts, not running any vms.
        // 2. Hosts running vms for this account and created by a service offering which uses an
        //    implicit dedication planner.
        // 3. Hosts running vms created by implicit planner and in strict mode of other accounts.
        // 4. Hosts running vms from other account or from this account but created by a service offering which uses
        //    any planner besides implicit.
        final Set<Long> emptyHosts = new HashSet<>();
        final Set<Long> hostRunningVmsOfAccount = new HashSet<>();
        final Set<Long> hostRunningStrictImplicitVmsOfOtherAccounts = new HashSet<>();
        final Set<Long> allOtherHosts = new HashSet<>();
        for (final Long host : allHosts) {
            final List<VMInstanceVO> vms = getVmsOnHost(host);
            if (vms == null || vms.isEmpty()) {
                emptyHosts.add(host);
            } else if (checkHostSuitabilityForImplicitDedication(account.getAccountId(), vms)) {
                hostRunningVmsOfAccount.add(host);
            } else if (checkIfAllVmsCreatedInStrictMode(account.getAccountId(), vms)) {
                hostRunningStrictImplicitVmsOfOtherAccounts.add(host);
            } else {
                allOtherHosts.add(host);
            }
        }

        // Hosts running vms of other accounts created by ab implicit planner in strict mode should always be avoided.
        avoid.addHostList(hostRunningStrictImplicitVmsOfOtherAccounts);

        if (!hostRunningVmsOfAccount.isEmpty() && (hostsToAvoid == null || !hostsToAvoid.containsAll(hostRunningVmsOfAccount))) {
            // Check if any of hosts that are running implicit dedicated vms are available (not in avoid list).
            // If so, we'll try and use these hosts.
            avoid.addHostList(emptyHosts);
            avoid.addHostList(allOtherHosts);
            clusterList = getUpdatedClusterList(clusterList, avoid.getHostsToAvoid());
        } else if (!emptyHosts.isEmpty() && (hostsToAvoid == null || !hostsToAvoid.containsAll(emptyHosts))) {
            // If there aren't implicit resources try on empty hosts
            avoid.addHostList(allOtherHosts);
            clusterList = getUpdatedClusterList(clusterList, avoid.getHostsToAvoid());
        } else if (!preferred) {
            // If in strict mode, there is nothing else to try.
            clusterList = null;
        } else {
            // If in preferred mode, check if hosts are available to try, otherwise return an empty cluster list.
            if (!allOtherHosts.isEmpty() && (hostsToAvoid == null || !hostsToAvoid.containsAll(allOtherHosts))) {
                clusterList = getUpdatedClusterList(clusterList, avoid.getHostsToAvoid());
            } else {
                clusterList = null;
            }
        }

        return clusterList;
    }

    private boolean isServiceOfferingUsingPlannerInPreferredMode(final long serviceOfferingId) {
        boolean preferred = false;
        final Map<String, String> details = serviceOfferingDetailsDao.listDetailsKeyPairs(serviceOfferingId);
        if (details != null && !details.isEmpty()) {
            final String preferredAttribute = details.get("ImplicitDedicationMode");
            if (preferredAttribute != null && preferredAttribute.equals("Preferred")) {
                preferred = true;
            }
        }
        return preferred;
    }

    private List<VMInstanceVO> getVmsOnHost(final long hostId) {
        final List<VMInstanceVO> vms = vmInstanceDao.listUpByHostId(hostId);
        final List<VMInstanceVO> vmsByLastHostId = vmInstanceDao.listByLastHostId(hostId);
        if (vmsByLastHostId.size() > 0) {
            // check if any VMs are within skip.counting.hours, if yes we have to consider the host.
            for (final VMInstanceVO stoppedVM : vmsByLastHostId) {
                final long secondsSinceLastUpdate = (DateUtil.currentGMTTime().getTime() - stoppedVM.getUpdateTime().getTime()) / 1000;
                if (secondsSinceLastUpdate < capacityReleaseInterval) {
                    vms.add(stoppedVM);
                }
            }
        }

        return vms;
    }

    private boolean checkHostSuitabilityForImplicitDedication(final Long accountId, final List<VMInstanceVO> allVmsOnHost) {
        boolean suitable = true;
        if (allVmsOnHost.isEmpty()) {
            return false;
        }

        for (final VMInstanceVO vm : allVmsOnHost) {
            if (vm.getAccountId() != accountId) {
                s_logger.info("Host " + vm.getHostId() + " found to be unsuitable for implicit dedication as it is " + "running instances of another account");
                suitable = false;
                break;
            } else {
                if (!isImplicitPlannerUsedByOffering(vm.getServiceOfferingId())) {
                    s_logger.info("Host " + vm.getHostId() + " found to be unsuitable for implicit dedication as it " +
                            "is running instances of this account which haven't been created using implicit dedication.");
                    suitable = false;
                    break;
                }
            }
        }
        return suitable;
    }

    private boolean checkIfAllVmsCreatedInStrictMode(final Long accountId, final List<VMInstanceVO> allVmsOnHost) {
        boolean createdByImplicitStrict = true;
        if (allVmsOnHost.isEmpty()) {
            return false;
        }
        for (final VMInstanceVO vm : allVmsOnHost) {
            if (!isImplicitPlannerUsedByOffering(vm.getServiceOfferingId())) {
                s_logger.info("Host " + vm.getHostId() + " found to be running a vm created by a planner other" + " than implicit.");
                createdByImplicitStrict = false;
                break;
            } else if (isServiceOfferingUsingPlannerInPreferredMode(vm.getServiceOfferingId())) {
                s_logger.info("Host " + vm.getHostId() + " found to be running a vm created by an implicit planner" + " in preferred mode.");
                createdByImplicitStrict = false;
                break;
            }
        }
        return createdByImplicitStrict;
    }

    private List<Long> getUpdatedClusterList(final List<Long> clusterList, final Set<Long> hostsSet) {
        final List<Long> updatedClusterList = new ArrayList<>();
        for (final Long cluster : clusterList) {
            final List<HostVO> hosts = resourceMgr.listAllHostsInCluster(cluster);
            final Set<Long> hostsInClusterSet = new HashSet<>();
            for (final HostVO host : hosts) {
                hostsInClusterSet.add(host.getId());
            }

            if (!hostsSet.containsAll(hostsInClusterSet)) {
                updatedClusterList.add(cluster);
            }
        }

        return updatedClusterList;
    }

    private boolean isImplicitPlannerUsedByOffering(final long offeringId) {
        boolean implicitPlannerUsed = false;
        final ServiceOfferingVO offering = serviceOfferingDao.findByIdIncludingRemoved(offeringId);
        if (offering == null) {
            s_logger.error("Couldn't retrieve the offering by the given id : " + offeringId);
        } else {
            String plannerName = offering.getDeploymentPlanner();
            if (plannerName == null) {
                plannerName = globalDeploymentPlanner;
            }

            if (plannerName != null && this.getName().equals(plannerName)) {
                implicitPlannerUsed = true;
            }
        }

        return implicitPlannerUsed;
    }

    @Override
    public PlannerResourceUsage getResourceUsage(final VirtualMachineProfile vmProfile, final DeploymentPlan plan, final ExcludeList avoid) throws
            InsufficientServerCapacityException {
        // Check if strict or preferred mode should be used.
        final boolean preferred = isServiceOfferingUsingPlannerInPreferredMode(vmProfile.getServiceOfferingId());

        // If service offering in strict mode return resource usage as Dedicated
        if (!preferred) {
            return PlannerResourceUsage.Dedicated;
        } else {
            // service offering is in implicit mode.
            // find is it possible to deploy in dedicated mode,
            // if its possible return dedicated else return shared.
            final List<Long> clusterList = super.orderClusters(vmProfile, plan, avoid);
            final Set<Long> hostsToAvoid = avoid.getHostsToAvoid();
            final Account account = vmProfile.getOwner();

            // Get the list of all the hosts in the given clusters
            final List<Long> allHosts = new ArrayList<>();
            for (final Long cluster : clusterList) {
                final List<HostVO> hostsInCluster = resourceMgr.listAllHostsInCluster(cluster);
                for (final HostVO hostVO : hostsInCluster) {

                    allHosts.add(hostVO.getId());
                }
            }

            // Go over all the hosts in the cluster and get a list of
            // 1. All empty hosts, not running any vms.
            // 2. Hosts running vms for this account and created by a service
            // offering which uses an
            // implicit dedication planner.
            // 3. Hosts running vms created by implicit planner and in strict
            // mode of other accounts.
            // 4. Hosts running vms from other account or from this account but
            // created by a service offering which uses
            // any planner besides implicit.
            final Set<Long> emptyHosts = new HashSet<>();
            final Set<Long> hostRunningVmsOfAccount = new HashSet<>();
            final Set<Long> hostRunningStrictImplicitVmsOfOtherAccounts = new HashSet<>();
            final Set<Long> allOtherHosts = new HashSet<>();
            for (final Long host : allHosts) {
                final List<VMInstanceVO> vms = getVmsOnHost(host);
                // emptyHost should contain only Hosts which are not having any VM's (user/system) on it.
                if (vms == null || vms.isEmpty()) {
                    emptyHosts.add(host);
                } else if (checkHostSuitabilityForImplicitDedication(account.getAccountId(), vms)) {
                    hostRunningVmsOfAccount.add(host);
                } else if (checkIfAllVmsCreatedInStrictMode(account.getAccountId(), vms)) {
                    hostRunningStrictImplicitVmsOfOtherAccounts.add(host);
                } else {
                    allOtherHosts.add(host);
                }
            }

            // Hosts running vms of other accounts created by ab implicit
            // planner in strict mode should always be avoided.
            avoid.addHostList(hostRunningStrictImplicitVmsOfOtherAccounts);

            if (!hostRunningVmsOfAccount.isEmpty() && (hostsToAvoid == null || !hostsToAvoid.containsAll(hostRunningVmsOfAccount))) {
                // Check if any of hosts that are running implicit dedicated vms are available (not in avoid list).
                // If so, we'll try and use these hosts. We can deploy in Dedicated mode
                return PlannerResourceUsage.Dedicated;
            } else if (!emptyHosts.isEmpty() && (hostsToAvoid == null || !hostsToAvoid.containsAll(emptyHosts))) {
                // If there aren't implicit resources try on empty hosts, As empty hosts are available we can deploy in Dedicated mode.
                // Empty hosts can contain hosts which are not having user vms but system vms are running.
                // But the host where system vms are running is marked as shared and still be part of empty Hosts.
                // The scenario will fail where actual Empty hosts and uservms not running host.
                return PlannerResourceUsage.Dedicated;
            } else {
                if (!allOtherHosts.isEmpty() && (hostsToAvoid == null || !hostsToAvoid.containsAll(allOtherHosts))) {
                    return PlannerResourceUsage.Shared;
                }
            }
            return PlannerResourceUsage.Shared;
        }
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);
        capacityReleaseInterval = NumbersUtil.parseInt(configDao.getValue(Config.CapacitySkipcountingHours.key()), 3600);
        return true;
    }
}
