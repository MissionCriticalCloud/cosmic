package com.cloud.cluster.agentlb;

import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClusterBasedAgentLoadBalancerPlanner extends AdapterBase implements AgentLoadBalancerPlanner {
    private static final Logger s_logger = LoggerFactory.getLogger(AgentLoadBalancerPlanner.class);

    @Inject
    HostDao _hostDao = null;

    @Override
    public List<HostVO> getHostsToRebalance(final long msId, final int avLoad) {
        QueryBuilder<HostVO> sc = QueryBuilder.create(HostVO.class);
        sc.and(sc.entity().getType(), Op.EQ, Host.Type.Routing);
        sc.and(sc.entity().getManagementServerId(), Op.EQ, msId);
        final List<HostVO> allHosts = sc.list();

        if (allHosts.size() <= avLoad) {
            s_logger.debug("Agent load = " + allHosts.size() + " for management server " + msId + " doesn't exceed average system agent load = " + avLoad +
                    "; so it doesn't participate in agent rebalancing process");
            return null;
        }

        sc = QueryBuilder.create(HostVO.class);
        sc.and(sc.entity().getManagementServerId(), Op.EQ, msId);
        sc.and(sc.entity().getType(), Op.EQ, Host.Type.Routing);
        sc.and(sc.entity().getStatus(), Op.EQ, Status.Up);
        final List<HostVO> directHosts = sc.list();

        if (directHosts.isEmpty()) {
            s_logger.debug("No direct agents in status " + Status.Up + " exist for the management server " + msId +
                    "; so it doesn't participate in agent rebalancing process");
            return null;
        }

        Map<Long, List<HostVO>> hostToClusterMap = new HashMap<>();

        for (final HostVO directHost : directHosts) {
            final Long clusterId = directHost.getClusterId();
            List<HostVO> directHostsPerCluster = null;
            if (!hostToClusterMap.containsKey(clusterId)) {
                directHostsPerCluster = new ArrayList<>();
            } else {
                directHostsPerCluster = hostToClusterMap.get(clusterId);
            }
            directHostsPerCluster.add(directHost);
            hostToClusterMap.put(clusterId, directHostsPerCluster);
        }

        hostToClusterMap = sortByClusterSize(hostToClusterMap);

        final int hostsToGive = allHosts.size() - avLoad;
        int hostsLeftToGive = hostsToGive;
        int hostsLeft = directHosts.size();
        final List<HostVO> hostsToReturn = new ArrayList<>();

        s_logger.debug("Management server " + msId + " can give away " + hostsToGive + " as it currently owns " + allHosts.size() +
                " and the average agent load in the system is " + avLoad + "; finalyzing list of hosts to give away...");
        for (final Long cluster : hostToClusterMap.keySet()) {
            final List<HostVO> hostsInCluster = hostToClusterMap.get(cluster);
            hostsLeft = hostsLeft - hostsInCluster.size();
            if (hostsToReturn.size() < hostsToGive) {
                s_logger.debug("Trying cluster id=" + cluster);

                if (hostsInCluster.size() > hostsLeftToGive) {
                    s_logger.debug("Skipping cluster id=" + cluster + " as it has more hosts than we need: " + hostsInCluster.size() + " vs " + hostsLeftToGive);
                    if (hostsLeft >= hostsLeftToGive) {
                        continue;
                    } else {
                        break;
                    }
                } else {
                    s_logger.debug("Taking all " + hostsInCluster.size() + " hosts: " + hostsInCluster + " from cluster id=" + cluster);
                    hostsToReturn.addAll(hostsInCluster);
                    hostsLeftToGive = hostsLeftToGive - hostsInCluster.size();
                }
            } else {
                break;
            }
        }

        s_logger.debug("Management server " + msId + " is ready to give away " + hostsToReturn.size() + " hosts");
        return hostsToReturn;
    }

    public static LinkedHashMap<Long, List<HostVO>> sortByClusterSize(final Map<Long, List<HostVO>> hostToClusterMap) {
        final List<Long> keys = new ArrayList<>();
        keys.addAll(hostToClusterMap.keySet());
        Collections.sort(keys, new Comparator<Long>() {
            @Override
            public int compare(final Long o1, final Long o2) {
                final List<HostVO> v1 = hostToClusterMap.get(o1);
                final List<HostVO> v2 = hostToClusterMap.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                }

                if (v1.size() < v2.size()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        final LinkedHashMap<Long, List<HostVO>> sortedMap = new LinkedHashMap<>();
        for (final Long key : keys) {
            sortedMap.put(key, hostToClusterMap.get(key));
        }
        return sortedMap;
    }
}
