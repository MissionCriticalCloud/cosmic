package com.cloud.api.commands;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.dedicated.DedicatedService;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "releaseDedicatedZone", group = APICommandGroup.ZoneService, description = "Release dedication of zone", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ReleaseDedicatedZoneCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ReleaseDedicatedZoneCmd.class.getName());

    private static final String s_name = "releasededicatedzoneresponse";
    @Inject
    DedicatedService dedicatedService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, required = true, description = "the ID of the Zone")
    private Long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = dedicatedService.releaseDedicatedResource(getZoneId(), null, null, null);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to release dedicated zone");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getZoneId() {
        return zoneId;
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
        return EventTypes.EVENT_DEDICATE_RESOURCE_RELEASE;
    }

    @Override
    public String getEventDescription() {
        return "releasing dedicated zone";
    }
}
