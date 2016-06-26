package org.apache.cloudstack.engine.service.api;

import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.storage.StoragePool;
import org.apache.cloudstack.engine.datacenter.entity.api.ClusterEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.HostEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.PodEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.StorageEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.ZoneEntity;

import java.util.List;
import java.util.Map;

/**
 * ProvisioningService registers and deregisters physical and virtual
 * resources that the management server can use.
 */
public interface ProvisioningService {

    StorageEntity registerStorage(String name, List<String> tags, Map<String, String> details);

    ZoneEntity registerZone(String zoneUuid, String name, String owner, List<String> tags, Map<String, String> details);

    PodEntity registerPod(String podUuid, String name, String owner, String zoneUuid, List<String> tags, Map<String, String> details);

    ClusterEntity registerCluster(String clusterUuid, String name, String owner, List<String> tags, Map<String, String> details);

    HostEntity registerHost(String uuid, String name, String owner, List<String> tags, Map<String, String> details);

    void deregisterStorage(String uuid);

    void deregisterZone(String uuid);

    void deregisterPod(String uuid);

    void deregisterCluster(String uuid);

    void deregisterHost(String uuid);

    void changeState(String type, String entity, Status state);

    List<Host> listHosts();

    List<PodEntity> listPods();

    List<ZoneEntity> listZones();

    List<StoragePool> listStorage();

    ZoneEntity getZone(String id);
}
