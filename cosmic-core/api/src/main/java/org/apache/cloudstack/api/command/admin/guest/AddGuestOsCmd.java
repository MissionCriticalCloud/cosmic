package org.apache.cloudstack.api.command.admin.guest;

import com.cloud.event.EventTypes;
import com.cloud.storage.GuestOS;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.GuestOSCategoryResponse;
import org.apache.cloudstack.api.response.GuestOSResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addGuestOs", description = "Add a new guest OS type", responseObject = GuestOSResponse.class,
        since = "4.4.0", requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddGuestOsCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddGuestOsCmd.class.getName());

    private static final String s_name = "addguestosresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.OS_CATEGORY_ID, type = CommandType.UUID, entityType = GuestOSCategoryResponse.class, required = true, description = "ID of Guest OS category")
    private Long osCategoryId;

    @Parameter(name = ApiConstants.OS_DISPLAY_NAME, type = CommandType.STRING, required = true, description = "Unique display name for Guest OS")
    private String osDisplayName;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = false, description = "Optional name for Guest OS")
    private String osName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getOsCategoryId() {
        return osCategoryId;
    }

    public String getOsDisplayName() {
        return osDisplayName;
    }

    public String getOsName() {
        return osName;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void create() {
        final GuestOS guestOs = _mgr.addGuestOs(this);
        if (guestOs != null) {
            setEntityId(guestOs.getId());
            setEntityUuid(guestOs.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add new guest OS type entity");
        }
    }

    @Override
    public String getCreateEventType() {
        return EventTypes.EVENT_GUEST_OS_ADD;
    }

    @Override
    public String getCreateEventDescription() {
        return "adding new guest OS type";
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Guest OS Id: " + getEntityId());
        final GuestOS guestOs = _mgr.getAddedGuestOs(getEntityId());
        if (guestOs != null) {
            final GuestOSResponse response = _responseGenerator.createGuestOSResponse(guestOs);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add new guest OS type");
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
        return EventTypes.EVENT_GUEST_OS_ADD;
    }

    @Override
    public String getEventDescription() {
        return "adding a new guest OS type Id: " + getEntityId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.GuestOs;
    }
}
