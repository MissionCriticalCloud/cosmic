package com.cloud.api.command.user.network;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.admin.network.DeleteNetworkOfferingCmd;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.Network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteNetwork", description = "Deletes a network", responseObject = SuccessResponse.class, entityType = {Network.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteNetworkCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteNetworkOfferingCmd.class.getName());
    private static final String s_name = "deletenetworkresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = NetworkResponse.class, required = true, description = "the ID of the network")
    private Long id;

    @Parameter(name = ApiConstants.FORCED, type = CommandType.BOOLEAN, required = false, description = "Force delete a network." +
            " Network will be marked as 'Destroy' even when commands to shutdown and cleanup to the backend fails.")
    private Boolean forced;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Network Id: " + id);
        final boolean result = _networkService.deleteNetwork(id, isForced());
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete network");
        }
    }

    public boolean isForced() {
        return (forced != null) ? forced : false;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Network network = _networkService.getNetwork(id);
        if (network == null) {
            throw new InvalidParameterValueException("Network ID=" + id + " doesn't exist");
        } else {
            return _networkService.getNetwork(id).getAccountId();
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting network: " + id;
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Network;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return id;
    }
}
