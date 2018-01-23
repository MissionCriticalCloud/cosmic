package com.cloud.api.command.admin.storage;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.storage.StoragePool;
import com.cloud.user.Account;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateStoragePool", group = APICommandGroup.StoragePoolService, description = "Updates a storage pool.", responseObject = StoragePoolResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateStoragePoolCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateStoragePoolCmd.class.getName());

    private static final String s_name = "updatestoragepoolresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = StoragePoolResponse.class, required = true, description = "the Id of the storage pool")
    private Long id;

    @Parameter(name = ApiConstants.TAGS, type = CommandType.LIST, collectionType = CommandType.STRING, description = "comma-separated list of tags for the storage pool")
    private List<String> tags;

    @Parameter(name = ApiConstants.CAPACITY_IOPS, type = CommandType.LONG, required = false, description = "IOPS CloudStack can provision from this storage pool")
    private Long capacityIops;

    @Parameter(name = ApiConstants.CAPACITY_BYTES, type = CommandType.LONG, required = false, description = "bytes CloudStack can provision from this storage pool")
    private Long capacityBytes;

    @Parameter(name = ApiConstants.ENABLED, type = CommandType.BOOLEAN, required = false, description = "false to disable the pool for allocation of new volumes, true to" +
            " enable it back.")
    private Boolean enabled;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }

    public Long getCapacityIops() {
        return capacityIops;
    }

    public Long getCapacityBytes() {
        return capacityBytes;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final StoragePool result = _storageService.updateStoragePool(this);
        if (result != null) {
            final StoragePoolResponse response = _responseGenerator.createStoragePoolResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update storage pool");
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
