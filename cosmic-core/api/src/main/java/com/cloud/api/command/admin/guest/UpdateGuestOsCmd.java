package com.cloud.api.command.admin.guest;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.GuestOSResponse;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.user.Account;
import com.cloud.storage.GuestOS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateGuestOs", group = APICommandGroup.GuestOSService, description = "Updates the information about Guest OS", responseObject = GuestOSResponse.class,
        since = "4.4.0", requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateGuestOsCmd extends BaseAsyncCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(UpdateGuestOsCmd.class.getName());

    private static final String s_name = "updateguestosresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = GuestOSResponse.class, required = true, description = "UUID of the Guest OS")
    private Long id;

    @Parameter(name = ApiConstants.OS_DISPLAY_NAME, type = CommandType.STRING, required = true, description = "Unique display name for Guest OS")
    private String osDisplayName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getOsDisplayName() {
        return osDisplayName;
    }

    @Override
    public void execute() {
        final GuestOS guestOs = _mgr.updateGuestOs(this);
        if (guestOs != null) {
            final GuestOSResponse response = _responseGenerator.createGuestOSResponse(guestOs);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update guest OS type");
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
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_GUEST_OS_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating guest OS: " + getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.GuestOs;
    }
}
