package com.cloud.api.command.user.volume;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.user.Account;
import com.cloud.storage.Volume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteVolume", group = APICommandGroup.VolumeService, description = "Deletes a detached disk volume.", responseObject = SuccessResponse.class, entityType = {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteVolumeCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteVolumeCmd.class.getName());
    private static final String s_name = "deletevolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VolumeResponse.class,
            required = true, description = "The ID of the disk volume")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "volume";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ConcurrentOperationException {
        CallContext.current().setEventDetails("Volume Id: " + getId());
        final boolean result = _volumeService.deleteVolume(id, CallContext.current().getCallingAccount());
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete volume");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Volume volume = _entityMgr.findById(Volume.class, getId());
        if (volume != null) {
            return volume.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getId() {
        return id;
    }
}
