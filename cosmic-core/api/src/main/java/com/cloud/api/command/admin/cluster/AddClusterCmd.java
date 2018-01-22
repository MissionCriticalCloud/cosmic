package com.cloud.api.command.admin.cluster;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.exception.DiscoveryException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.org.Cluster;
import com.cloud.user.Account;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addCluster", group = APICommandGroup.ClusterService, description = "Adds a new cluster", responseObject = ClusterResponse.class,
        requestHasSensitiveInfo = true, responseHasSensitiveInfo = false)
public class AddClusterCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddClusterCmd.class.getName());

    private static final String s_name = "addclusterresponse";

    @Parameter(name = ApiConstants.CLUSTER_NAME, type = CommandType.STRING, required = true, description = "the cluster name")
    private String clusterName;

    @Parameter(name = ApiConstants.PASSWORD, type = CommandType.STRING, required = false, description = "the password for the host")
    private String password;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, required = true, description = "the Pod ID for the host")
    private Long podId;

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, required = false, description = "the URL")
    private String url;

    @Parameter(name = ApiConstants.USERNAME, type = CommandType.STRING, required = false, description = "the username for the cluster")
    private String username;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, required = true, description = "the Zone ID for the cluster")
    private Long zoneId;

    @Parameter(name = ApiConstants.HYPERVISOR,
            type = CommandType.STRING,
            required = true,
            description = "hypervisor type of the cluster: XenServer,KVM")
    private String hypervisor;

    @Parameter(name = ApiConstants.CLUSTER_TYPE, type = CommandType.STRING, required = true, description = "type of the cluster: CloudManaged, ExternalManaged")
    private String clusterType;

    @Parameter(name = ApiConstants.ALLOCATION_STATE, type = CommandType.STRING, description = "Allocation state of this cluster for allocation of new resources")
    private String allocationState;

    public String getClusterName() {
        return clusterName;
    }

    public String getPassword() {
        return password;
    }

    public Long getPodId() {
        return podId;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(final String type) {
        clusterType = type;
    }

    public String getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final String allocationState) {
        this.allocationState = allocationState;
    }

    @Override
    public void execute() {
        try {
            final List<? extends Cluster> result = _resourceService.discoverCluster(this);
            final ListResponse<ClusterResponse> response = new ListResponse<>();
            final List<ClusterResponse> clusterResponses = new ArrayList<>();
            if (result != null && result.size() > 0) {
                for (final Cluster cluster : result) {
                    final ClusterResponse clusterResponse = _responseGenerator.createClusterResponse(cluster, false);
                    clusterResponses.add(clusterResponse);
                }
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add cluster");
            }

            response.setResponses(clusterResponses);
            response.setResponseName(getCommandName());

            setResponseObject(response);
        } catch (final DiscoveryException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (final ResourceInUseException ex) {
            s_logger.warn("Exception: ", ex);
            final ServerApiException e = new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
            for (final String proxyObj : ex.getIdProxyList()) {
                e.addProxyObject(proxyObj);
            }
            throw e;
        }
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
