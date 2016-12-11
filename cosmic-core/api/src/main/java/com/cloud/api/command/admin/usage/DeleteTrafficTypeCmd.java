package com.cloud.api.command.admin.usage;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.TrafficTypeResponse;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteTrafficType", description = "Deletes traffic type of a physical network", responseObject = SuccessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteTrafficTypeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteTrafficTypeCmd.class.getName());

    private static final String s_name = "deletetraffictyperesponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TrafficTypeResponse.class, required = true, description = "traffic type id")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = _networkService.deletePhysicalNetworkTrafficType(getId());
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete traffic type");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_TRAFFIC_TYPE_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting Traffic Type: " + getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.TrafficType;
    }
}
