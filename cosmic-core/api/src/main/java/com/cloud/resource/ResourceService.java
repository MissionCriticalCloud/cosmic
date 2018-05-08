package com.cloud.resource;

import com.cloud.api.command.admin.cluster.AddClusterCmd;
import com.cloud.api.command.admin.cluster.DeleteClusterCmd;
import com.cloud.api.command.admin.host.AddHostCmd;
import com.cloud.api.command.admin.host.CancelMaintenanceCmd;
import com.cloud.api.command.admin.host.PrepareForMaintenanceCmd;
import com.cloud.api.command.admin.host.ReconnectHostCmd;
import com.cloud.api.command.admin.host.UpdateHostCmd;
import com.cloud.api.command.admin.host.UpdateHostPasswordCmd;
import com.cloud.legacymodel.dc.Cluster;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.exceptions.DiscoveryException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.NoTransitionException;
import com.cloud.legacymodel.exceptions.ResourceInUseException;
import com.cloud.model.enumeration.HypervisorType;

import java.util.List;

public interface ResourceService {
    /**
     * Updates a host
     *
     * @param cmd - the command specifying hostId
     * @return hostObject
     * @throws NoTransitionException
     */
    Host updateHost(UpdateHostCmd cmd) throws NoTransitionException;

    Host cancelMaintenance(CancelMaintenanceCmd cmd);

    Host reconnectHost(ReconnectHostCmd cmd);

    /**
     * We will automatically create a cloud.com cluster to attach to the external cluster and return a hyper host to perform
     * host related operation within the cluster
     *
     * @param cmd
     * @return
     * @throws IllegalArgumentException
     * @throws DiscoveryException
     */
    List<? extends Cluster> discoverCluster(AddClusterCmd cmd) throws IllegalArgumentException, DiscoveryException, ResourceInUseException;

    boolean deleteCluster(DeleteClusterCmd cmd);

    Cluster updateCluster(Cluster cluster, String clusterType, String hypervisor, String allocationState, String managedstate);

    List<? extends Host> discoverHosts(AddHostCmd cmd) throws IllegalArgumentException, DiscoveryException, InvalidParameterValueException;

    Host maintain(PrepareForMaintenanceCmd cmd);

    /**
     * Deletes a host
     *
     * @param hostId   TODO
     * @param isForced TODO
     * @param true     if deleted, false otherwise
     */
    boolean deleteHost(long hostId, boolean isForced, boolean isForceDeleteStorage);

    boolean updateClusterPassword(UpdateHostPasswordCmd upasscmd);

    boolean updateHostPassword(UpdateHostPasswordCmd upasscmd);

    Host getHost(long hostId);

    Cluster getCluster(Long clusterId);

    List<HypervisorType> getSupportedHypervisorTypes(long zoneId, boolean forVirtualRouter, Long podId);

    boolean releaseHostReservation(Long hostId);
}
