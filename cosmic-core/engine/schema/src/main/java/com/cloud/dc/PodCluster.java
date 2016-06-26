package com.cloud.dc;

public class PodCluster {
    HostPodVO _pod;
    ClusterVO _cluster;

    protected PodCluster() {
        super();
    }

    public PodCluster(final HostPodVO pod, final ClusterVO cluster) {
        _pod = pod;
        _cluster = cluster;
    }

    public HostPodVO getPod() {
        return _pod;
    }

    public ClusterVO getCluster() {
        return _cluster;
    }

    @Override
    public int hashCode() {
        return _pod.hashCode() ^ (_cluster != null ? _cluster.hashCode() : 0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PodCluster)) {
            return false;
        }

        final PodCluster that = (PodCluster) obj;
        if (!this._pod.equals(that._pod)) {
            return false;
        }

        if (this._cluster == null && that._cluster == null) {
            return true;
        }

        if (this._cluster == null || that._cluster == null) {
            return false;
        }

        return this._cluster.equals(that._cluster);
    }
}
