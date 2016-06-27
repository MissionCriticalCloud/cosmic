package org.apache.cloudstack.engine.service.api;

import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.storage.StoragePool;
import org.apache.cloudstack.engine.datacenter.entity.api.ClusterEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.ClusterEntityImpl;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceManager;
import org.apache.cloudstack.engine.datacenter.entity.api.HostEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.HostEntityImpl;
import org.apache.cloudstack.engine.datacenter.entity.api.PodEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.PodEntityImpl;
import org.apache.cloudstack.engine.datacenter.entity.api.StorageEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.ZoneEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.ZoneEntityImpl;

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
    public StorageEntity registerStorage(final String name, final List<String> tags, final Map<String, String> details) {
        // TODO Auto-generated method stub
        return null;
    }

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
    public void deregisterStorage(final String uuid) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deregisterZone(final String uuid) {
        final ZoneEntityImpl zoneEntity = new ZoneEntityImpl(uuid, manager);
        zoneEntity.disable();
    }

    @Override
    public void deregisterPod(final String uuid) {
        final PodEntityImpl podEntity = new PodEntityImpl(uuid, manager);
        podEntity.disable();
    }

    @Override
    public void deregisterCluster(final String uuid) {
        final ClusterEntityImpl clusterEntity = new ClusterEntityImpl(uuid, manager);
        clusterEntity.disable();
    }

    @Override
    public void deregisterHost(final String uuid) {
        final HostEntityImpl hostEntity = new HostEntityImpl(uuid, manager);
        hostEntity.disable();
    }

    @Override
    public void changeState(final String type, final String entity, final Status state) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Host> listHosts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PodEntity> listPods() {
        /*
         * Not in use now, just commented out.
         */
        //List<PodEntity> pods = new ArrayList<PodEntity>();
        //pods.add(new PodEntityImpl("pod-uuid-1", "pod1"));
        //pods.add(new PodEntityImpl("pod-uuid-2", "pod2"));
        return null;
    }

    @Override
    public List<ZoneEntity> listZones() {
        final List<ZoneEntity> zones = new ArrayList<>();
        //zones.add(new ZoneEntityImpl("zone-uuid-1"));
        //zones.add(new ZoneEntityImpl("zone-uuid-2"));
        return zones;
    }

    @Override
    public List<StoragePool> listStorage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ZoneEntity getZone(final String uuid) {
        final ZoneEntityImpl impl = new ZoneEntityImpl(uuid, manager);
        return impl;
    }
}
