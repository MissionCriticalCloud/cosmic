package com.cloud.deploy;

import com.cloud.db.model.Zone;
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
import java.util.Set;
import java.util.TreeSet;

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

    enum AllocationAlgorithm {
        random, firstfit, userdispersing, userconcentratedpod_random, userconcentratedpod_firstfit
    }

    enum PlannerResourceUsage {
        Shared, Dedicated
    }

    class ExcludeList implements Serializable {
        private final Set<Long> zoneIds = new TreeSet<>();
        private final Set<Long> podIds = new TreeSet<>();
        private final Set<Long> clusterIds = new TreeSet<>();
        private final Set<Long> hostIds = new TreeSet<>();
        private final Set<Long> poolIds = new TreeSet<>();

        public ExcludeList() {
        }

        public ExcludeList(final Set<Long> zoneIds, final Set<Long> podIds, final Set<Long> clusterIds, final Set<Long> hostIds, final Set<Long> poolIds) {
            this.zoneIds.addAll(zoneIds);
            this.podIds.addAll(podIds);
            this.clusterIds.addAll(clusterIds);
            this.hostIds.addAll(hostIds);
            this.poolIds.addAll(poolIds);
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
            } else if (Zone.class.isAssignableFrom(scope)) {
                addZone(e.getId());
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
            hostIds.add(hostId);
        }

        public void addPod(final long podId) {
            podIds.add(podId);
        }

        public void addZone(final long zoneId) {
            zoneIds.add(zoneId);
        }

        public void addCluster(final long clusterId) {
            clusterIds.add(clusterId);
        }

        public void addPool(final long poolId) {
            poolIds.add(poolId);
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
            } else if (Zone.class.isAssignableFrom(scope)) {
                addZone(e.getResourceId());
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
            podIds.addAll(podList);
        }

        public void addClusterList(final Collection<Long> clusterList) {
            clusterIds.addAll(clusterList);
        }

        public void addHostList(final Collection<Long> hostList) {
            hostIds.addAll(hostList);
        }

        public boolean shouldAvoid(final Zone zone) {
            return shouldAvoid(zone.getId(), null, null, null, null);
        }

        public boolean shouldAvoid(final Pod pod) {
            return shouldAvoid(pod.getDataCenterId(), pod.getId(), null, null, null);
        }

        public boolean shouldAvoid(final Cluster cluster) {
            return shouldAvoid(cluster.getDataCenterId(), cluster.getPodId(), cluster.getId(), null, null);
        }

        public boolean shouldAvoid(final Host host) {
            return shouldAvoid(host.getDataCenterId(), host.getPodId(), host.getClusterId(), null, host.getId());
        }

        public boolean shouldAvoid(final StoragePool pool) {
            return shouldAvoid(pool.getDataCenterId(), pool.getPodId(), pool.getClusterId(), pool.getId(), null);
        }

        private boolean shouldAvoid(final Long zoneId, final Long podId, final Long clusterId, final Long poolId, final Long hostId) {
            if (zoneId != null && !zoneIds.isEmpty() && zoneIds.contains(zoneId)) {
                return true;
            }

            if (podId != null && !podIds.isEmpty() && podIds.contains(podId)) {
                return true;
            }

            if (clusterId != null && !clusterIds.isEmpty() && clusterIds.contains(clusterId)) {
                return true;
            }

            if (poolId != null && !poolIds.isEmpty() && poolIds.contains(poolId)) {
                return true;
            }

            if (hostId != null && !hostIds.isEmpty() && hostIds.contains(hostId)) {
                return true;
            }

            return false;
        }

        public Set<Long> getZonesToAvoid() {
            return zoneIds;
        }

        public Set<Long> getPodsToAvoid() {
            return podIds;
        }

        public Set<Long> getClustersToAvoid() {
            return clusterIds;
        }

        public Set<Long> getHostsToAvoid() {
            return hostIds;
        }

        public Set<Long> getPoolsToAvoid() {
            return poolIds;
        }

        @Override
        public String toString() {
            return "avoid: zones " + zoneIds + ", pods " + podIds + ", clusters " + clusterIds + ", hosts " + hostIds + ", pools " + poolIds;
        }
    }
}
