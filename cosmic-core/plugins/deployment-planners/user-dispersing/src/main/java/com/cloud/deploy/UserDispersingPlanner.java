package com.cloud.deploy;

import com.cloud.configuration.Config;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachineProfile;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDispersingPlanner extends FirstFitPlanner implements DeploymentClusterPlanner {

    private static final Logger s_logger = LoggerFactory.getLogger(UserDispersingPlanner.class);
    float _userDispersionWeight;

    /**
     * This method should reorder the given list of Cluster Ids by applying any necessary heuristic
     * for this planner
     * For UserDispersingPlanner we need to order the clusters by considering the number of VMs for this account
     *
     * @return List<Long> ordered list of Cluster Ids
     */
    @Override
    protected List<Long> reorderClusters(final long id, final boolean isZone, final Pair<List<Long>, Map<Long, Double>> clusterCapacityInfo, final VirtualMachineProfile vmProfile,
                                         final DeploymentPlan plan) {
        final List<Long> clusterIdsByCapacity = clusterCapacityInfo.first();
        if (vmProfile.getOwner() == null) {
            return clusterIdsByCapacity;
        }
        final long accountId = vmProfile.getOwner().getAccountId();
        final Pair<List<Long>, Map<Long, Double>> clusterIdsVmCountInfo = listClustersByUserDispersion(id, isZone, accountId);

        //now we have 2 cluster lists - one ordered by capacity and the other by number of VMs for this account
        //need to apply weights to these to find the correct ordering to follow

        if (_userDispersionWeight == 1.0f) {
            final List<Long> clusterIds = clusterIdsVmCountInfo.first();
            clusterIds.retainAll(clusterIdsByCapacity);
            return clusterIds;
        } else {
            //apply weights to the two lists
            return orderByApplyingWeights(clusterCapacityInfo, clusterIdsVmCountInfo, accountId);
        }
    }

    /**
     * This method should reorder the given list of Pod Ids by applying any necessary heuristic
     * for this planner
     * For UserDispersingPlanner we need to order the pods by considering the number of VMs for this account
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

        final Pair<List<Long>, Map<Long, Double>> podIdsVmCountInfo = listPodsByUserDispersion(plan.getDataCenterId(), accountId);

        //now we have 2 pod lists - one ordered by capacity and the other by number of VMs for this account
        //need to apply weights to these to find the correct ordering to follow

        if (_userDispersionWeight == 1.0f) {
            final List<Long> podIds = podIdsVmCountInfo.first();
            podIds.retainAll(podIdsByCapacity);
            return podIds;
        } else {
            //apply weights to the two lists
            return orderByApplyingWeights(podCapacityInfo, podIdsVmCountInfo, accountId);
        }
    }

    protected Pair<List<Long>, Map<Long, Double>> listPodsByUserDispersion(final long dataCenterId, final long accountId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Applying Userdispersion heuristic to pods for account: " + accountId);
        }
        final Pair<List<Long>, Map<Long, Double>> podIdsVmCountInfo = vmInstanceDao.listPodIdsInZoneByVmCount(dataCenterId, accountId);
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("List of pods in ascending order of number of VMs: " + podIdsVmCountInfo.first());
        }

        return podIdsVmCountInfo;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        final String weight = configDao.getValue(Config.VmUserDispersionWeight.key());
        _userDispersionWeight = NumbersUtil.parseFloat(weight, 1.0f);

        return true;
    }

    protected Pair<List<Long>, Map<Long, Double>> listClustersByUserDispersion(final long id, final boolean isZone, final long accountId) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Applying Userdispersion heuristic to clusters for account: " + accountId);
        }
        final Pair<List<Long>, Map<Long, Double>> clusterIdsVmCountInfo;
        if (isZone) {
            clusterIdsVmCountInfo = vmInstanceDao.listClusterIdsInZoneByVmCount(id, accountId);
        } else {
            clusterIdsVmCountInfo = vmInstanceDao.listClusterIdsInPodByVmCount(id, accountId);
        }
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("List of clusters in ascending order of number of VMs: " + clusterIdsVmCountInfo.first());
        }
        return clusterIdsVmCountInfo;
    }

    private List<Long> orderByApplyingWeights(final Pair<List<Long>, Map<Long, Double>> capacityInfo, final Pair<List<Long>, Map<Long, Double>> vmCountInfo, final long accountId) {
        final List<Long> capacityOrderedIds = capacityInfo.first();
        final List<Long> vmCountOrderedIds = vmCountInfo.first();
        final Map<Long, Double> capacityMap = capacityInfo.second();
        final Map<Long, Double> vmCountMap = vmCountInfo.second();

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Capacity Id list: " + capacityOrderedIds + " , capacityMap:" + capacityMap);
        }
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Vm Count Id list: " + vmCountOrderedIds + " , vmCountMap:" + vmCountMap);
        }

        final List<Long> idsReorderedByWeights = new ArrayList<>();
        final float capacityWeight = (1.0f - _userDispersionWeight);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Applying userDispersionWeight: " + _userDispersionWeight);
        }
        //normalize the vmCountMap
        final LinkedHashMap<Long, Double> normalisedVmCountIdMap = new LinkedHashMap<>();

        final Long totalVmsOfAccount = vmInstanceDao.countRunningByAccount(accountId);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Total VMs for account: " + totalVmsOfAccount);
        }
        for (final Long id : vmCountOrderedIds) {
            final Double normalisedCount = vmCountMap.get(id) / totalVmsOfAccount;
            normalisedVmCountIdMap.put(id, normalisedCount);
        }

        //consider only those ids that are in capacity map.

        final SortedMap<Double, List<Long>> sortedMap = new TreeMap<>();
        for (final Long id : capacityOrderedIds) {
            final Double weightedCapacityValue = capacityMap.get(id) * capacityWeight;
            final Double weightedVmCountValue = normalisedVmCountIdMap.get(id) * _userDispersionWeight;
            final Double totalWeight = weightedCapacityValue + weightedVmCountValue;
            if (sortedMap.containsKey(totalWeight)) {
                final List<Long> idList = sortedMap.get(totalWeight);
                idList.add(id);
                sortedMap.put(totalWeight, idList);
            } else {
                final List<Long> idList = new ArrayList<>();
                idList.add(id);
                sortedMap.put(totalWeight, idList);
            }
        }

        for (final List<Long> idList : sortedMap.values()) {
            idsReorderedByWeights.addAll(idList);
        }

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Reordered Id list: " + idsReorderedByWeights);
        }

        return idsReorderedByWeights;
    }
}
