package com.cloud.api.command.user.vpc;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.vpc.Vpc;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteVPC", description = "Deletes a VPC", responseObject = SuccessResponse.class, entityType = {Vpc.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteVPCCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteVPCCmd.class.getName());
    private static final String s_name = "deletevpcresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VpcResponse.class, required = true, description = "the ID of the VPC")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VPC_DELETE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Deleting VPC id=" + getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.vpcSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getId();
    }

    @Override
    public void execute() {
        try {
            final boolean result = _vpcService.deleteVpc(getId());
            if (result) {
                final SuccessResponse response = new SuccessResponse(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete VPC");
            }
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Vpc vpc = _entityMgr.findById(Vpc.class, getId());
        if (vpc != null) {
            return vpc.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
