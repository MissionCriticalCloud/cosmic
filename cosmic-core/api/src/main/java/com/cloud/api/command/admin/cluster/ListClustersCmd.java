package com.cloud.api.command.admin.cluster;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.command.user.offering.ListServiceOfferingsCmd;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.legacymodel.dc.Cluster;
import com.cloud.legacymodel.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listClusters", group = APICommandGroup.ClusterService, description = "Lists clusters.", responseObject = ClusterResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListClustersCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListServiceOfferingsCmd.class.getName());

    private static final String s_name = "listclustersresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ClusterResponse.class, description = "lists clusters by the cluster ID")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "lists clusters by the cluster name")
    private String clusterName;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "lists clusters by Pod ID")
    private Long podId;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "lists clusters by Zone ID")
    private Long zoneId;

    @Parameter(name = ApiConstants.HYPERVISOR, type = CommandType.STRING, description = "lists clusters by hypervisor type")
    private String hypervisorType;

    @Parameter(name = ApiConstants.CLUSTER_TYPE, type = CommandType.STRING, description = "lists clusters by cluster type")
    private String clusterType;

    @Parameter(name = ApiConstants.ALLOCATION_STATE, type = CommandType.STRING, description = "lists clusters by allocation state")
    private String allocationState;

    @Parameter(name = ApiConstants.MANAGED_STATE, type = CommandType.STRING, description = "whether this cluster is managed by cloudstack")
    private String managedState;

    @Parameter(name = ApiConstants.SHOW_CAPACITIES, type = CommandType.BOOLEAN, description = "flag to display the capacity of the clusters")
    private Boolean showCapacities;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getHypervisorType() {
        return hypervisorType;
    }

    public String getClusterType() {
        return clusterType;
    }

    public String getAllocationState() {
        return allocationState;
    }

    public String getManagedstate() {
        return managedState;
    }

    public void setManagedstate(final String managedstate) {
        this.managedState = managedstate;
    }

    public Boolean getShowCapacities() {
        return showCapacities;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends Cluster>, Integer> result = _mgr.searchForClusters(this);
        final ListResponse<ClusterResponse> response = new ListResponse<>();
        final List<ClusterResponse> clusterResponses = new ArrayList<>();
        for (final Cluster cluster : result.first()) {
            final ClusterResponse clusterResponse = _responseGenerator.createClusterResponse(cluster, showCapacities);
            clusterResponse.setObjectName("cluster");
            clusterResponses.add(clusterResponse);
        }

        response.setResponses(clusterResponses, result.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
