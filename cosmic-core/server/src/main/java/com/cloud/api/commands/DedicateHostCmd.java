package com.cloud.api.commands;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DedicateHostResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.dedicated.DedicatedService;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.dc.DedicatedResources;
import com.cloud.legacymodel.user.Account;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "dedicateHost", group = APICommandGroup.HostService, description = "Dedicates a host.", responseObject = DedicateHostResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DedicateHostCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DedicateHostCmd.class.getName());
    private static final String s_name = "dedicatehostresponse";
    @Inject
    DedicatedService dedicatedService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.HOST_ID, type = CommandType.UUID, entityType = HostResponse.class, required = true, description = "the ID of the host to update")
    private Long hostId;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            required = true,
            description = "the ID of the containing domain")
    private Long domainId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "the name of the account which needs dedication. Must be used with domainId.")
    private String accountName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<? extends DedicatedResources> result = dedicatedService.dedicateHost(getHostId(), getDomainId(), getAccountName());
        final ListResponse<DedicateHostResponse> response = new ListResponse<>();
        final List<DedicateHostResponse> hostResponseList = new ArrayList<>();
        if (result != null) {
            for (final DedicatedResources resource : result) {
                final DedicateHostResponse hostResponse = dedicatedService.createDedicateHostResponse(resource);
                hostResponseList.add(hostResponse);
            }
            response.setResponses(hostResponseList);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to dedicate host");
        }
    }

    public Long getHostId() {
        return hostId;
    }

    public Long getDomainId() {
        return domainId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
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
        return EventTypes.EVENT_DEDICATE_RESOURCE;
    }

    @Override
    public String getEventDescription() {
        return "dedicating a host";
    }
}
