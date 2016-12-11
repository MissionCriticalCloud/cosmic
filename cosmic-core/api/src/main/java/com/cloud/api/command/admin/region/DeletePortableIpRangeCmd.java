package com.cloud.api.command.admin.region;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.PortableIpRangeResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deletePortableIpRange", description = "deletes a range of portable public IP's associated with a region", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeletePortableIpRangeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeletePortableIpRangeCmd.class.getName());

    private static final String s_name = "deleteportablepublicipresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, required = true, entityType = PortableIpRangeResponse.class, description = "Id of the portable ip range")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = _configService.deletePortableIpRange(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete portable ip range");
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

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PORTABLE_IP_RANGE_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "deleting a portable public ip range";
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.PortableIpAddress;
    }
}
