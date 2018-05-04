package com.cloud.api.command.admin.storage;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.legacymodel.exceptions.ResourceInUseException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.user.Account;

import java.net.UnknownHostException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createStoragePool", group = APICommandGroup.StoragePoolService, description = "Creates a storage pool.", responseObject = StoragePoolResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateStoragePoolCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateStoragePoolCmd.class.getName());

    private static final String s_name = "createstoragepoolresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.CLUSTER_ID, type = CommandType.UUID, entityType = ClusterResponse.class, description = "the cluster ID for the storage pool")
    private Long clusterId;

    @Parameter(name = ApiConstants.DETAILS, type = CommandType.MAP, description = "the details for the storage pool")
    private Map details;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "the name for the storage pool")
    private String storagePoolName;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "the Pod ID for the storage pool")
    private Long podId;

    @Parameter(name = ApiConstants.TAGS, type = CommandType.STRING, description = "the tags for the storage pool")
    private String tags;

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, required = true, description = "the URL of the storage pool")
    private String url;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, required = true, description = "the Zone ID for the storage pool")
    private Long zoneId;

    @Parameter(name = ApiConstants.PROVIDER, type = CommandType.STRING, required = false, description = "the storage provider name")
    private String storageProviderName;

    @Parameter(name = ApiConstants.SCOPE, type = CommandType.STRING, required = false, description = "the scope of the storage: cluster or zone")
    private String scope;

    @Parameter(name = ApiConstants.MANAGED, type = CommandType.BOOLEAN, required = false, description = "whether the storage should be managed by CloudStack")
    private Boolean managed;

    @Parameter(name = ApiConstants.CAPACITY_IOPS, type = CommandType.LONG, required = false, description = "IOPS CloudStack can provision from this storage pool")
    private Long capacityIops;

    @Parameter(name = ApiConstants.CAPACITY_BYTES, type = CommandType.LONG, required = false, description = "bytes CloudStack can provision from this storage pool")
    private Long capacityBytes;

    @Parameter(name = ApiConstants.HYPERVISOR,
            type = CommandType.STRING,
            required = false,
            description = "hypervisor type of the hosts in zone that will be attached to this storage pool. KVM supported as of now.")
    private String hypervisor;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getClusterId() {
        return clusterId;
    }

    public Map getDetails() {
        return details;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public Long getPodId() {
        return podId;
    }

    public String getTags() {
        return tags;
    }

    public String getUrl() {
        return url;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getStorageProviderName() {
        return storageProviderName;
    }

    public String getScope() {
        return scope;
    }

    public Boolean isManaged() {
        return managed;
    }

    public Long getCapacityIops() {
        return capacityIops;
    }

    public Long getCapacityBytes() {
        return capacityBytes;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    @Override
    public void execute() {
        try {
            final StoragePool result = _storageService.createPool(this);
            if (result != null) {
                final StoragePoolResponse response = _responseGenerator.createStoragePoolResponse(result);
                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add storage pool");
            }
        } catch (final ResourceUnavailableException ex1) {
            s_logger.warn("Exception: ", ex1);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex1.getMessage());
        } catch (final ResourceInUseException ex2) {
            s_logger.warn("Exception: ", ex2);
            throw new ServerApiException(ApiErrorCode.RESOURCE_IN_USE_ERROR, ex2.getMessage());
        } catch (final UnknownHostException ex3) {
            s_logger.warn("Exception: ", ex3);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex3.getMessage());
        } catch (final Exception ex4) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex4.getMessage());
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
