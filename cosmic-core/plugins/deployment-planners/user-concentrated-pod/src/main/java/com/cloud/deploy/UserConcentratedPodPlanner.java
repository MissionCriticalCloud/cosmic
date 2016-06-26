package com.cloud.deploy;

import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachineProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConcentratedPodPlanner extends FirstFitPlanner implements DeploymentClusterPlanner {

    private static final Logger s_logger = LoggerFactory.getLogger(UserConcentratedPodPlanner.class);

    /**
     * This method should reorder the given list of Cluster Ids by applying any necessary heuristic
     * for this planner
     * For UserConcentratedPodPlanner we need to order the clusters in a zone across pods, by considering those pods first which have more number of VMs for this account
     * This reordering is not done incase the clusters within single pod are passed when the allocation is applied at pod-level.
     *
     * @return List<Long> ordered list of Cluster Ids
     */
    @Override
    protected List<Long> reorderClusters(final long id, final boolean isZone, final Pair<List<Long>, Map<Long, Double>> clusterCapacityInfo, final VirtualMachineProfile vmProfile,
                                         final DeploymentPlan plan) {
        final List<Long> clusterIdsByCapacity = clusterCapacityInfo.first();
        if (vmProfile.getOwner() == null || !isZone) {
            return clusterIdsByCapacity;
        }
        return applyUserConcentrationPodHeuristicToClusters(id, clusterIdsByCapacity, vmProfile.getOwner().getAccountId());
    }

    private List<Long> applyUserConcentrationPodHeuristicToClusters(final long zoneId, final List<Long> prioritizedClusterIds, final long accountId) {
        //user has VMs in certain pods. - prioritize those pods first
        //UserConcentratedPod strategy
        List<Long> clusterList = new ArrayList<>();
        final List<Long> podIds = listPodsByUserConcentration(zoneId, accountId);
        if (!podIds.isEmpty()) {
            clusterList = reorderClustersByPods(prioritizedClusterIds, podIds);
        } else {
            clusterList = prioritizedClusterIds;
        }
        return clusterList;
    }

    protected List<Long> listPodsByUserConcentration(final long zoneId, final long accountId) {

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Applying UserConcentratedPod heuristic for account: " + accountId);
        }

        final List<Long> prioritizedPods = vmDao.listPodIdsHavingVmsforAccount(zoneId, accountId);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("List of pods to be considered, after applying UserConcentratedPod heuristic: " + prioritizedPods);
        }

        return prioritizedPods;
    }

    private List<Long> reorderClustersByPods(final List<Long> clusterIds, final List<Long> podIds) {

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Reordering cluster list as per pods ordered by user concentration");
        }

        final Map<Long, List<Long>> podClusterMap = clusterDao.getPodClusterIdMap(clusterIds);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Pod To cluster Map is: " + podClusterMap);
        }

        final List<Long> reorderedClusters = new ArrayList<>();
        for (final Long pod : podIds) {
            if (podClusterMap.containsKey(pod)) {
                final List<Long> clustersOfThisPod = podClusterMap.get(pod);
                if (clustersOfThisPod != null) {
                    for (final Long clusterId : clusterIds) {
                        if (clustersOfThisPod.contains(clusterId)) {
                            reorderedClusters.add(clusterId);
                        }
                    }
                    clusterIds.removeAll(clustersOfThisPod);
                }
            }
        }
        reorderedClusters.addAll(clusterIds);

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Reordered cluster list: " + reorderedClusters);
        }
        return reorderedClusters;
    }

    /**
     * This method should reorder the given list of Pod Ids by applying any necessary heuristic
     * for this planner
     * For UserConcentratedPodPlanner we need to order the pods by considering those pods first which have more number of VMs for this account
     *
     * @return List<Long> ordered list of Pod Ids
     */
    @Override
    protected List<Long> reorderPods(final Pair<List<Long>, Map<Long, Double>> podCapacityInfo, final VirtualMachineProfile vmProfile, final DeploymentPlan plan) {
        final List<Long> podIdsByCapacity = podCapacityInfo.first();
        if (vmProfile.getOwner() == null) {
            return podIdsByCapacity;
        }
        final long accountId = vmProfile.getOwner().getAccountId();

        //user has VMs in certain pods. - prioritize those pods first
        //UserConcentratedPod strategy
        final List<Long> podIds = listPodsByUserConcentration(plan.getDataCenterId(), accountId);
        if (!podIds.isEmpty()) {
            //remove pods that dont have capacity for this vm
            podIds.retainAll(podIdsByCapacity);
            podIdsByCapacity.removeAll(podIds);
            podIds.addAll(podIdsByCapacity);
            return podIds;
        } else {
            return podIdsByCapacity;
        }
    }
}
