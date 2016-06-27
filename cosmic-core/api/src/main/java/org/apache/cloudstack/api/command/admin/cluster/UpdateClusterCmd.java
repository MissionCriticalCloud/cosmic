package org.apache.cloudstack.api.command.admin.cluster;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.org.Cluster;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ClusterResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateCluster", description = "Updates an existing cluster", responseObject = ClusterResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateClusterCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddClusterCmd.class.getName());

    private static final String s_name = "updateclusterresponse";

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ClusterResponse.class, required = true, description = "the ID of the Cluster")
    private Long id;

    @Parameter(name = ApiConstants.CLUSTER_NAME, type = CommandType.STRING, description = "the cluster name")
    private String clusterName;

    @Parameter(name = ApiConstants.HYPERVISOR, type = CommandType.STRING, description = "hypervisor type of the cluster")
    private String hypervisor;

    @Parameter(name = ApiConstants.CLUSTER_TYPE, type = CommandType.STRING, description = "hypervisor type of the cluster")
    private String clusterType;

    @Parameter(name = ApiConstants.ALLOCATION_STATE, type = CommandType.STRING, description = "Allocation state of this cluster for allocation of new resources")
    private String allocationState;

    @Parameter(name = ApiConstants.MANAGED_STATE, type = CommandType.STRING, description = "whether this cluster is managed by cloudstack")
    private String managedState;

    public String getClusterName() {
        return clusterName;
    }

    @Override
    public void execute() {
        final Cluster cluster = _resourceService.getCluster(getId());
        if (cluster == null) {
            throw new InvalidParameterValueException("Unable to find the cluster by id=" + getId());
        }
        final Cluster result = _resourceService.updateCluster(cluster, getClusterType(), getHypervisor(), getAllocationState(), getManagedstate());
        if (result != null) {
            final ClusterResponse clusterResponse = _responseGenerator.createClusterResponse(cluster, false);
            clusterResponse.setResponseName(getCommandName());
            this.setResponseObject(clusterResponse);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update cluster");
        }
    }

    public Long getId() {
        return id;
    }

    public String getClusterType() {
        return clusterType;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public String getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final String allocationState) {
        this.allocationState = allocationState;
    }

    public String getManagedstate() {
        return managedState;
    }

    public void setManagedstate(final String managedstate) {
        this.managedState = managedstate;
    }

    public void setClusterType(final String type) {
        this.clusterType = type;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
