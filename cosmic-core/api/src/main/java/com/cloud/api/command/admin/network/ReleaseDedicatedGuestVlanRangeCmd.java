package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.GuestVlanRangeResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "releaseDedicatedGuestVlanRange", group = APICommandGroup.VLANService, description = "Releases a dedicated guest vlan range to the system", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ReleaseDedicatedGuestVlanRangeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ReleaseDedicatedGuestVlanRangeCmd.class.getName());
    private static final String s_name = "releasededicatedguestvlanrangeresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = GuestVlanRangeResponse.class,
            required = true,
            description = "the ID of the dedicated guest vlan range")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_DEDICATED_GUEST_VLAN_RANGE_RELEASE;
    }

    @Override
    public String getEventDescription() {
        return "Releasing a dedicated guest vlan range.";
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.DedicatedGuestVlanRange;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Dedicated guest vlan range Id: " + id);
        final boolean result = _networkService.releaseDedicatedGuestVlanRange(getId());
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to release dedicated guest vlan range");
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

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }
}
