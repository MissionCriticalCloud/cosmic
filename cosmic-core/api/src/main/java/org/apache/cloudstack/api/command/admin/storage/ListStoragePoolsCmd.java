package org.apache.cloudstack.api.command.admin.storage;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ClusterResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listStoragePools", description = "Lists storage pools.", responseObject = StoragePoolResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListStoragePoolsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListStoragePoolsCmd.class.getName());

    private static final String s_name = "liststoragepoolsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.CLUSTER_ID,
            type = CommandType.UUID,
            entityType = ClusterResponse.class,
            description = "list storage pools belongig to the specific cluster")
    private Long clusterId;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, description = "the IP address for the storage pool")
    private String ipAddress;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the storage pool")
    private String storagePoolName;

    @Parameter(name = ApiConstants.PATH, type = CommandType.STRING, description = "the storage pool path")
    private String path;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "the Pod ID for the storage pool")
    private Long podId;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the Zone ID for the storage pool")
    private Long zoneId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = StoragePoolResponse.class, description = "the ID of the storage pool")
    private Long id;

    @Parameter(name = ApiConstants.SCOPE, type = CommandType.STRING, entityType = StoragePoolResponse.class, description = "the ID of the storage pool")
    private String scope;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getClusterId() {
        return clusterId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public String getPath() {
        return path;
    }

    public Long getPodId() {
        return podId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.StoragePool;
    }

    @Override
    public void execute() {
        final ListResponse<StoragePoolResponse> response = _queryService.searchForStoragePools(this);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    public String getScope() {
        return scope;
    }
}
