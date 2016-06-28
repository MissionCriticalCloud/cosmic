package org.apache.cloudstack.engine.service.api;

import org.apache.cloudstack.engine.datacenter.entity.api.ClusterEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.HostEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.PodEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.ZoneEntity;

import java.util.List;
import java.util.Map;

/**
 * ProvisioningService registers and deregisters physical and virtual
 * resources that the management server can use.
 */
public interface ProvisioningService {

    ZoneEntity registerZone(String zoneUuid, String name, String owner, List<String> tags, Map<String, String> details);

    PodEntity registerPod(String podUuid, String name, String owner, String zoneUuid, List<String> tags, Map<String, String> details);

    ClusterEntity registerCluster(String clusterUuid, String name, String owner, List<String> tags, Map<String, String> details);

    HostEntity registerHost(String uuid, String name, String owner, List<String> tags, Map<String, String> details);

    List<ZoneEntity> listZones();

    ZoneEntity getZone(String id);
}
