package com.cloud.api.command.user.template;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.StorageUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.template.VirtualMachineTemplate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "copyTemplate", group = APICommandGroup.TemplateService, description = "Copies a template from one zone to another.", responseObject = TemplateResponse.class, responseView =
        ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CopyTemplateCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CopyTemplateCmd.class.getName());
    private static final String s_name = "copytemplateresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.DESTINATION_ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            required = true,
            description = "ID of the zone the template is being copied to.")
    private Long destZoneId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TemplateResponse.class, required = true, description = "Template ID.")
    private Long id;

    @Parameter(name = ApiConstants.SOURCE_ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            description = "ID of the zone the template is currently hosted on. If not specified and template is cross-zone, then we will sync this template to region wide image " +
                    "store.")
    private Long sourceZoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getStaticName() {
        return s_name;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_TEMPLATE_COPY;
    }

    @Override
    public String getEventDescription() {
        return "copying template: " + getId() + " from zone: " + getSourceZoneId() + " to zone: " + getDestinationZoneId();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Template;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() throws ResourceAllocationException {
        try {
            CallContext.current().setEventDetails(getEventDescription());
            final VirtualMachineTemplate template = _templateService.copyTemplate(this);

            if (template != null) {
                final List<TemplateResponse> listResponse = _responseGenerator.createTemplateResponses(ResponseView.Restricted, template, getDestinationZoneId(), false);
                TemplateResponse response = new TemplateResponse();
                if (listResponse != null && !listResponse.isEmpty()) {
                    response = listResponse.get(0);
                }

                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to copy template");
            }
        } catch (final StorageUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        }
    }

    public Long getDestinationZoneId() {
        return destZoneId;
    }

    public Long getSourceZoneId() {
        return sourceZoneId;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final VirtualMachineTemplate template = _entityMgr.findById(VirtualMachineTemplate.class, getId());
        if (template != null) {
            return template.getAccountId();
        }

        // bad id given, parent this command to SYSTEM so ERROR events are tracked
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
