package com.cloud.api.command.user.iso;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.user.Account;
import com.cloud.template.VirtualMachineTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteIso", group = APICommandGroup.ISOService, description = "Deletes an ISO file.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteIsoCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteIsoCmd.class.getName());
    private static final String s_name = "deleteisoresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TemplateResponse.class, required = true, description = "the ID of the ISO file")
    private Long id;

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            description = "the ID of the zone of the ISO file. If not specified, the ISO will be deleted from all the zones")
    private Long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getStaticName() {
        return s_name;
    }

    public Long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ISO_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting ISO " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Iso;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("ISO Id: " + getId());
        final boolean result = _templateService.deleteIso(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete ISO");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final VirtualMachineTemplate iso = _entityMgr.findById(VirtualMachineTemplate.class, getId());
        if (iso != null) {
            return iso.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM;
    }
}
