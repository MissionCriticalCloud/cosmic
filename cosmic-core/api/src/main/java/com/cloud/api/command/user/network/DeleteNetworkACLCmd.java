package com.cloud.api.command.user.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NetworkACLItemResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteNetworkACL", group = APICommandGroup.NetworkACLService, description = "Deletes a network ACL", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteNetworkACLCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteNetworkACLCmd.class.getName());
    private static final String s_name = "deletenetworkaclresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = NetworkACLItemResponse.class, required = true, description = "the ID of the network ACL")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_ACL_ITEM_DELETE;
    }

    @Override
    public String getEventDescription() {
        return ("Deleting Network ACL ID=" + id);
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        CallContext.current().setEventDetails("Network ACL item ID: " + id);
        final boolean result = _networkACLService.revokeNetworkACLItem(id);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete network ACL item");
        }
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
        final Account caller = CallContext.current().getCallingAccount();
        return caller.getAccountId();
    }
}
