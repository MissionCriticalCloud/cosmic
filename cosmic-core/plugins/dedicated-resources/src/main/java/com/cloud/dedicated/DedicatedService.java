package com.cloud.dedicated;

import com.cloud.api.commands.ListDedicatedClustersCmd;
import com.cloud.api.commands.ListDedicatedHostsCmd;
import com.cloud.api.commands.ListDedicatedPodsCmd;
import com.cloud.api.commands.ListDedicatedZonesCmd;
import com.cloud.api.response.DedicateClusterResponse;
import com.cloud.api.response.DedicateHostResponse;
import com.cloud.api.response.DedicatePodResponse;
import com.cloud.api.response.DedicateZoneResponse;
import com.cloud.dc.DedicatedResourceVO;
import com.cloud.dc.DedicatedResources;
import com.cloud.utils.Pair;
import com.cloud.utils.component.PluggableService;

import java.util.List;

public interface DedicatedService extends PluggableService {

    DedicatePodResponse createDedicatePodResponse(DedicatedResources resource);

    DedicateClusterResponse createDedicateClusterResponse(DedicatedResources resource);

    DedicateHostResponse createDedicateHostResponse(DedicatedResources resource);

    Pair<List<? extends DedicatedResourceVO>, Integer> listDedicatedPods(ListDedicatedPodsCmd cmd);

    Pair<List<? extends DedicatedResourceVO>, Integer> listDedicatedHosts(ListDedicatedHostsCmd cmd);

    Pair<List<? extends DedicatedResourceVO>, Integer> listDedicatedClusters(ListDedicatedClustersCmd cmd);

    boolean releaseDedicatedResource(Long zoneId, Long podId, Long clusterId, Long hostId);

    DedicateZoneResponse createDedicateZoneResponse(DedicatedResources resource);

    Pair<List<? extends DedicatedResourceVO>, Integer> listDedicatedZones(ListDedicatedZonesCmd cmd);

    List<DedicatedResourceVO> dedicateZone(Long zoneId, Long domainId, String accountName);

    List<DedicatedResourceVO> dedicatePod(Long podId, Long domainId, String accountName);

    List<DedicatedResourceVO> dedicateCluster(Long clusterId, Long domainId, String accountName);

    List<DedicatedResourceVO> dedicateHost(Long hostId, Long domainId, String accountName);
}
