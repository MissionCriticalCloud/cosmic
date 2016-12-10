package com.cloud.engine.service.api;

import com.cloud.engine.datacenter.entity.api.ClusterEntity;
import com.cloud.engine.datacenter.entity.api.ClusterEntityImpl;
import com.cloud.engine.datacenter.entity.api.DataCenterResourceManager;
import com.cloud.engine.datacenter.entity.api.HostEntity;
import com.cloud.engine.datacenter.entity.api.HostEntityImpl;
import com.cloud.engine.datacenter.entity.api.PodEntity;
import com.cloud.engine.datacenter.entity.api.PodEntityImpl;
import com.cloud.engine.datacenter.entity.api.ZoneEntity;
import com.cloud.engine.datacenter.entity.api.ZoneEntityImpl;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service("provisioningService")
@Path("/provisioning")
public class ProvisioningServiceImpl implements ProvisioningService {

    @Inject
    DataCenterResourceManager manager;

    @Override
    public ZoneEntity registerZone(final String zoneUuid, final String name, final String owner, final List<String> tags, final Map<String, String> details) {
        final ZoneEntityImpl zoneEntity = new ZoneEntityImpl(zoneUuid, manager);
        zoneEntity.setName(name);
        zoneEntity.setOwner(owner);
        zoneEntity.setDetails(details);
        zoneEntity.persist();
        return zoneEntity;
    }

    @Override
    public PodEntity registerPod(final String podUuid, final String name, final String owner, final String zoneUuid, final List<String> tags, final Map<String, String> details) {
        final PodEntityImpl podEntity = new PodEntityImpl(podUuid, manager);
        podEntity.setOwner(owner);
        podEntity.setName(name);
        podEntity.persist();
        return podEntity;
    }

    @Override
    public ClusterEntity registerCluster(final String clusterUuid, final String name, final String owner, final List<String> tags, final Map<String, String> details) {
        final ClusterEntityImpl clusterEntity = new ClusterEntityImpl(clusterUuid, manager);
        clusterEntity.setOwner(owner);
        clusterEntity.setName(name);
        clusterEntity.persist();
        return clusterEntity;
    }

    @Override
    public HostEntity registerHost(final String hostUuid, final String name, final String owner, final List<String> tags, final Map<String, String> details) {
        final HostEntityImpl hostEntity = new HostEntityImpl(hostUuid, manager);
        hostEntity.setOwner(owner);
        hostEntity.setName(name);
        hostEntity.setDetails(details);

        hostEntity.persist();
        return hostEntity;
    }

    @Override
    public List<ZoneEntity> listZones() {
        return new ArrayList<>();
    }

    @Override
    public ZoneEntity getZone(final String uuid) {
        return new ZoneEntityImpl(uuid, manager);
    }
}
