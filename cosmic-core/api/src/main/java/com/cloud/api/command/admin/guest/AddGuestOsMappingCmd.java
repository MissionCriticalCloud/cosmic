package com.cloud.api.command.admin.guest;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.GuestOSResponse;
import com.cloud.api.response.GuestOsMappingResponse;
import com.cloud.event.EventTypes;
import com.cloud.storage.GuestOSHypervisor;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addGuestOsMapping", description = "Adds a guest OS name to hypervisor OS name mapping", responseObject = GuestOsMappingResponse.class,
        since = "4.4.0", requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddGuestOsMappingCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddGuestOsMappingCmd.class.getName());

    private static final String s_name = "addguestosmappingresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.OS_TYPE_ID, type = CommandType.UUID, required = false, entityType = GuestOSResponse.class, description = "UUID of Guest OS type. Either the " +
            "UUID or Display Name must be passed")
    private Long osTypeId;

    @Parameter(name = ApiConstants.OS_DISPLAY_NAME, type = CommandType.STRING, required = false, description = "Display Name of Guest OS standard type. Either Display Name or " +
            "UUID must be passed")
    private String osStdName;

    @Parameter(name = ApiConstants.HYPERVISOR, type = CommandType.STRING, required = true, description = "Hypervisor type. One of : XenServer, KVM")
    private String hypervisor;

    @Parameter(name = ApiConstants.HYPERVISOR_VERSION, type = CommandType.STRING, required = true, description = "Hypervisor version to create the mapping for. Use 'default' for" +
            " default versions")
    private String hypervisorVersion;

    @Parameter(name = ApiConstants.OS_NAME_FOR_HYPERVISOR, type = CommandType.STRING, required = true, description = "OS name specific to the hypervisor")
    private String osNameForHypervisor;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getOsTypeId() {
        return osTypeId;
    }

    public String getOsStdName() {
        return osStdName;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    public String getOsNameForHypervisor() {
        return osNameForHypervisor;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void create() {
        final GuestOSHypervisor guestOsMapping = _mgr.addGuestOsMapping(this);
        if (guestOsMapping != null) {
            setEntityId(guestOsMapping.getId());
            setEntityUuid(guestOsMapping.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add guest OS mapping entity");
        }
    }

    @Override
    public String getCreateEventType() {
        return EventTypes.EVENT_GUEST_OS_MAPPING_ADD;
    }

    @Override
    public String getCreateEventDescription() {
        return "adding new guest OS mapping";
    }

    @Override
    public void execute() {
        final GuestOSHypervisor guestOsMapping = _mgr.getAddedGuestOsMapping(getEntityId());
        if (guestOsMapping != null) {
            final GuestOsMappingResponse response = _responseGenerator.createGuestOSMappingResponse(guestOsMapping);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add guest OS mapping");
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
        return EventTypes.EVENT_GUEST_OS_MAPPING_ADD;
    }

    @Override
    public String getEventDescription() {
        return "adding a new guest OS mapping Id: " + getEntityId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.GuestOsMapping;
    }
}
