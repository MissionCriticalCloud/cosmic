package com.cloud.vm;

import com.cloud.dao.EntityManager;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.deploy.DeployDestination;
import com.cloud.host.Host;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.org.Cluster;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;

import java.util.HashMap;
import java.util.Map;

public class VmWorkMigrate extends VmWork {
    static private EntityManager s_entityMgr;
    static private ZoneRepository s_zoneRepository;
    private final Map<String, String> storage;
    Long zoneId;
    Long podId;
    Long clusterId;
    Long hostId;
    long srcHostId;

    public VmWorkMigrate(final long userId, final long accountId, final long vmId, final String handlerName,
                         final long srcHostId, final DeployDestination dst) {
        super(userId, accountId, vmId, handlerName);
        this.srcHostId = srcHostId;
        zoneId = dst.getZone() != null ? dst.getZone().getId() : null;
        podId = dst.getPod() != null ? dst.getPod().getId() : null;
        clusterId = dst.getCluster() != null ? dst.getCluster().getId() : null;
        hostId = dst.getHost() != null ? dst.getHost().getId() : null;
        if (dst.getStorageForDisks() != null) {
            storage = new HashMap<>(dst.getStorageForDisks().size());
            for (final Map.Entry<Volume, StoragePool> entry : dst.getStorageForDisks().entrySet()) {
                storage.put(entry.getKey().getUuid(), entry.getValue().getUuid());
            }
        } else {
            storage = null;
        }
    }

    static public void init(final EntityManager entityMgr, final ZoneRepository zoneRepository) {
        s_entityMgr = entityMgr;
        s_zoneRepository = zoneRepository;
    }

    public DeployDestination getDeployDestination() {
        final Zone zone = zoneId != null ? s_zoneRepository.findById(zoneId).orElse(null) : null;
        final Pod pod = podId != null ? s_entityMgr.findById(Pod.class, podId) : null;
        final Cluster cluster = clusterId != null ? s_entityMgr.findById(Cluster.class, clusterId) : null;
        final Host host = hostId != null ? s_entityMgr.findById(Host.class, hostId) : null;

        Map<Volume, StoragePool> vols = null;

        if (storage != null) {
            vols = new HashMap<>(storage.size());
            for (final Map.Entry<String, String> entry : storage.entrySet()) {
                vols.put(s_entityMgr.findByUuid(Volume.class, entry.getKey()), s_entityMgr.findByUuid(StoragePool.class, entry.getValue()));
            }
        }

        final DeployDestination dest = new DeployDestination(zone, pod, cluster, host, vols);
        return dest;
    }

    public long getSrcHostId() {
        return srcHostId;
    }
}
