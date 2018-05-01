package com.cloud.deploy;

import com.cloud.db.model.Zone;
import com.cloud.host.Host;
import com.cloud.legacymodel.dc.Cluster;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.storage.Volume;
import com.cloud.utils.NumbersUtil;

import java.io.Serializable;
import java.util.Map;

public class DeployDestination implements Serializable {

    Zone _zone;
    Pod _pod;
    Cluster _cluster;
    Host _host;
    Map<Volume, StoragePool> _storage;

    public DeployDestination(final Zone zone, final Pod pod, final Cluster cluster, final Host host, final Map<Volume, StoragePool> storage) {
        this(zone, pod, cluster, host);
        _storage = storage;
    }

    public DeployDestination(final Zone zone, final Pod pod, final Cluster cluster, final Host host) {
        _zone = zone;
        _pod = pod;
        _cluster = cluster;
        _host = host;
    }

    public DeployDestination() {
    }

    public Zone getZone() {
        return _zone;
    }

    public Pod getPod() {
        return _pod;
    }

    public Cluster getCluster() {
        return _cluster;
    }

    public Host getHost() {
        return _host;
    }

    public Map<Volume, StoragePool> getStorageForDisks() {
        return _storage;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(_host.getId());
    }

    @Override
    public boolean equals(final Object obj) {
        final DeployDestination that = (DeployDestination) obj;
        if (_zone == null || that._zone == null) {
            return false;
        }
        if (_zone.getId() != that._zone.getId()) {
            return false;
        }
        if (_pod == null || that._pod == null) {
            return false;
        }
        if (_pod.getId() != that._pod.getId()) {
            return false;
        }
        if (_cluster == null || that._cluster == null) {
            return false;
        }
        if (_cluster.getId() != that._cluster.getId()) {
            return false;
        }
        if (_host == null || that._host == null) {
            return false;
        }
        return _host.getId() == that._host.getId();
    }

    @Override
    public String toString() {

        Long zoneId = null;
        Long podId = null;
        Long clusterId = null;
        Long hostId = null;

        if (_zone != null) {
            zoneId = _zone.getId();
        }

        if (_pod != null) {
            podId = _pod.getId();
        }

        if (_cluster != null) {
            clusterId = _cluster.getId();
        }

        if (_host != null) {
            hostId = _host.getId();
        }

        final StringBuilder destination = new StringBuilder("Dest[Zone(Id)-Pod(Id)-Cluster(Id)-Host(Id)-Storage(Volume(Id|Type-->Pool(Id))] : Dest[");
        destination.append("Zone(").append(zoneId).append(")").append("-");
        destination.append("Pod(").append(podId).append(")").append("-");
        destination.append("Cluster(").append(clusterId).append(")").append("-");
        destination.append("Host(").append(hostId).append(")").append("-");
        destination.append("Storage(");
        if (_storage != null) {
            final StringBuffer storageBuf = new StringBuffer();
            //String storageStr = "";
            for (final Volume vol : _storage.keySet()) {
                if (!storageBuf.toString().equals("")) {
                    storageBuf.append(storageBuf.toString());
                    storageBuf.append(", ");
                }
                storageBuf.append(storageBuf);
                storageBuf.append("Volume(");
                storageBuf.append(vol.getId());
                storageBuf.append("|");
                storageBuf.append(vol.getVolumeType().name());
                storageBuf.append("-->Pool(");
                storageBuf.append(_storage.get(vol).getId());
                storageBuf.append(")");
            }
            destination.append(storageBuf.toString());
        }
        return destination.append(")]").toString();
    }
}
