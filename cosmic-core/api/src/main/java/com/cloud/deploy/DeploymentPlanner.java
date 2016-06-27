package com.cloud.deploy;

import com.cloud.dc.DataCenter;
import com.cloud.dc.Pod;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.Host;
import com.cloud.org.Cluster;
import com.cloud.storage.StoragePool;
import com.cloud.utils.component.Adapter;
import com.cloud.vm.VirtualMachineProfile;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 */
public interface DeploymentPlanner extends Adapter {

    /**
     * plan is called to determine where a virtual machine should be running.
     *
     * @param vm    virtual machine.
     * @param plan  deployment plan that tells you where it's being deployed to.
     * @param avoid avoid these data centers, pods, clusters, or hosts.
     * @return DeployDestination for that virtual machine.
     */
    @Deprecated
    DeployDestination plan(VirtualMachineProfile vm, DeploymentPlan plan, ExcludeList avoid) throws InsufficientServerCapacityException;

    /**
     * canHandle is called before plan to determine if the plan can do the allocation. Planers should be exclusive so
     * planner writer must
     * make sure only one planer->canHandle return true in the planner list
     *
     * @param vm    virtual machine.
     * @param plan  deployment plan that tells you where it's being deployed to.
     * @param avoid avoid these data centers, pods, clusters, or hosts.
     * @return true if it's okay to allocate; false or not
     */
    boolean canHandle(VirtualMachineProfile vm, DeploymentPlan plan, ExcludeList avoid);

    public enum AllocationAlgorithm {
        random, firstfit, userdispersing, userconcentratedpod_random, userconcentratedpod_firstfit
    }

    public enum PlannerResourceUsage {
        Shared, Dedicated
    }

    public static class ExcludeList implements Serializable {
        private static final long serialVersionUID = -482175549460148301L;

        private Set<Long> _dcIds;
        private Set<Long> _podIds;
        private Set<Long> _clusterIds;
        private Set<Long> _hostIds;
        private Set<Long> _poolIds;

        public ExcludeList() {
        }

        public ExcludeList(final Set<Long> dcIds, final Set<Long> podIds, final Set<Long> clusterIds, final Set<Long> hostIds, final Set<Long> poolIds) {
            if (dcIds != null) {
                this._dcIds = new HashSet<>(dcIds);
            }
            if (podIds != null) {
                this._podIds = new HashSet<>(podIds);
            }
            if (clusterIds != null) {
                this._clusterIds = new HashSet<>(clusterIds);
            }

            if (hostIds != null) {
                this._hostIds = new HashSet<>(hostIds);
            }
            if (poolIds != null) {
                this._poolIds = new HashSet<>(poolIds);
            }
        }

        public boolean add(final InsufficientCapacityException e) {
            final Class<?> scope = e.getScope();

            if (scope == null) {
                return false;
            }

            if (Host.class.isAssignableFrom(scope)) {
                addHost(e.getId());
            } else if (Pod.class.isAssignableFrom(scope)) {
                addPod(e.getId());
            } else if (DataCenter.class.isAssignableFrom(scope)) {
                addDataCenter(e.getId());
            } else if (Cluster.class.isAssignableFrom(scope)) {
                addCluster(e.getId());
            } else if (StoragePool.class.isAssignableFrom(scope)) {
                addPool(e.getId());
            } else {
                return false;
            }

            return true;
        }

        public void addHost(final long hostId) {
            if (_hostIds == null) {
                _hostIds = new HashSet<>();
            }
            _hostIds.add(hostId);
        }

        public void addPod(final long podId) {
            if (_podIds == null) {
                _podIds = new HashSet<>();
            }
            _podIds.add(podId);
        }

        public void addDataCenter(final long dataCenterId) {
            if (_dcIds == null) {
                _dcIds = new HashSet<>();
            }
            _dcIds.add(dataCenterId);
        }

        public void addCluster(final long clusterId) {
            if (_clusterIds == null) {
                _clusterIds = new HashSet<>();
            }
            _clusterIds.add(clusterId);
        }

        public void addPool(final long poolId) {
            if (_poolIds == null) {
                _poolIds = new HashSet<>();
            }
            _poolIds.add(poolId);
        }

        public boolean add(final ResourceUnavailableException e) {
            final Class<?> scope = e.getScope();

            if (scope == null) {
                return false;
            }

            if (Host.class.isAssignableFrom(scope)) {
                addHost(e.getResourceId());
            } else if (Pod.class.isAssignableFrom(scope)) {
                addPod(e.getResourceId());
            } else if (DataCenter.class.isAssignableFrom(scope)) {
                addDataCenter(e.getResourceId());
            } else if (Cluster.class.isAssignableFrom(scope)) {
                addCluster(e.getResourceId());
            } else if (StoragePool.class.isAssignableFrom(scope)) {
                addPool(e.getResourceId());
            } else {
                return false;
            }

            return true;
        }

        public void addPodList(final Collection<Long> podList) {
            if (_podIds == null) {
                _podIds = new HashSet<>();
            }
            _podIds.addAll(podList);
        }

        public void addClusterList(final Collection<Long> clusterList) {
            if (_clusterIds == null) {
                _clusterIds = new HashSet<>();
            }
            _clusterIds.addAll(clusterList);
        }

        public void addHostList(final Collection<Long> hostList) {
            if (_hostIds == null) {
                _hostIds = new HashSet<>();
            }
            _hostIds.addAll(hostList);
        }

        public boolean shouldAvoid(final Host host) {
            if (_dcIds != null && _dcIds.contains(host.getDataCenterId())) {
                return true;
            }

            if (_podIds != null && _podIds.contains(host.getPodId())) {
                return true;
            }

            if (_clusterIds != null && _clusterIds.contains(host.getClusterId())) {
                return true;
            }

            if (_hostIds != null && _hostIds.contains(host.getId())) {
                return true;
            }

            return false;
        }

        public boolean shouldAvoid(final Cluster cluster) {
            if (_dcIds != null && _dcIds.contains(cluster.getDataCenterId())) {
                return true;
            }

            if (_podIds != null && _podIds.contains(cluster.getPodId())) {
                return true;
            }

            if (_clusterIds != null && _clusterIds.contains(cluster.getId())) {
                return true;
            }
            return false;
        }

        public boolean shouldAvoid(final Pod pod) {
            if (_dcIds != null && _dcIds.contains(pod.getDataCenterId())) {
                return true;
            }

            if (_podIds != null && _podIds.contains(pod.getId())) {
                return true;
            }

            return false;
        }

        public boolean shouldAvoid(final StoragePool pool) {
            if (_dcIds != null && _dcIds.contains(pool.getDataCenterId())) {
                return true;
            }

            if (_podIds != null && _podIds.contains(pool.getPodId())) {
                return true;
            }

            if (_clusterIds != null && _clusterIds.contains(pool.getClusterId())) {
                return true;
            }

            if (_poolIds != null && _poolIds.contains(pool.getId())) {
                return true;
            }

            return false;
        }

        public boolean shouldAvoid(final DataCenter dc) {
            if (_dcIds != null && _dcIds.contains(dc.getId())) {
                return true;
            }
            return false;
        }

        public Set<Long> getDataCentersToAvoid() {
            return _dcIds;
        }

        public Set<Long> getPodsToAvoid() {
            return _podIds;
        }

        public Set<Long> getClustersToAvoid() {
            return _clusterIds;
        }

        public Set<Long> getHostsToAvoid() {
            return _hostIds;
        }

        public Set<Long> getPoolsToAvoid() {
            return _poolIds;
        }
    }
}
