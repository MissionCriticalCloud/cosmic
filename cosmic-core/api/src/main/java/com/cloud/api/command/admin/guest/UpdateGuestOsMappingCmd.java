package com.cloud.api.command.admin.guest;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.GuestOsMappingResponse;
import com.cloud.event.EventTypes;
import com.cloud.storage.GuestOSHypervisor;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateGuestOsMapping", description = "Updates the information about Guest OS to Hypervisor specific name mapping", responseObject = GuestOsMappingResponse
        .class,
        since = "4.4.0", requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateGuestOsMappingCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateGuestOsMappingCmd.class.getName());

    private static final String s_name = "updateguestosmappingresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = GuestOsMappingResponse.class, required = true, description = "UUID of the Guest OS to hypervisor " +
            "name Mapping")
    private Long id;

    @Parameter(name = ApiConstants.OS_NAME_FOR_HYPERVISOR, type = CommandType.STRING, required = true, description = "Hypervisor specific name for this Guest OS")
    private String osNameForHypervisor;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getOsNameForHypervisor() {
        return osNameForHypervisor;
    }

    @Override
    public void execute() {
        final GuestOSHypervisor guestOsMapping = _mgr.updateGuestOsMapping(this);
        if (guestOsMapping != null) {
            final GuestOsMappingResponse response = _responseGenerator.createGuestOSMappingResponse(guestOsMapping);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update guest OS mapping");
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
        return EventTypes.EVENT_GUEST_OS_MAPPING_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating Guest OS Mapping: " + getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.GuestOsMapping;
    }
}
