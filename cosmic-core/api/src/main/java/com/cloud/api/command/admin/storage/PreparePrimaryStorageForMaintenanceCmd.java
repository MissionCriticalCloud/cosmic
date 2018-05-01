package com.cloud.api.command.admin.storage;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.storage.StoragePool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "enableStorageMaintenance", group = APICommandGroup.StoragePoolService, description = "Puts storage pool into maintenance state", responseObject = StoragePoolResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class PreparePrimaryStorageForMaintenanceCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(PreparePrimaryStorageForMaintenanceCmd.class.getName());
    private static final String s_name = "prepareprimarystorageformaintenanceresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = StoragePoolResponse.class, required = true, description = "Primary storage ID")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "primarystorage";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_MAINTENANCE_PREPARE_PRIMARY_STORAGE;
    }

    @Override
    public String getEventDescription() {
        return "preparing storage pool: " + getId() + " for maintenance";
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.StoragePool;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException {
        final StoragePool result = _storageService.preparePrimaryStorageForMaintenance(getId());
        if (result != null) {
            final StoragePoolResponse response = _responseGenerator.createStoragePoolResponse(result);
            response.setResponseName("storagepool");
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to prepare primary storage for maintenance");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account account = CallContext.current().getCallingAccount();
        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
