package org.apache.cloudstack.api.command.admin.network;

import com.cloud.event.EventTypes;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.GuestVlanRangeResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "releaseDedicatedGuestVlanRange", description = "Releases a dedicated guest vlan range to the system", responseObject = SuccessResponse.class,
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
