package com.cloud.api.command.admin.domain;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.domain.Domain;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.user.Account;
import com.cloud.region.RegionService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteDomain", group = APICommandGroup.DomainService, description = "Deletes a specified domain", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteDomainCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteDomainCmd.class.getName());
    private static final String s_name = "deletedomainresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Inject
    RegionService _regionService;
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = DomainResponse.class, required = true, description = "ID of domain to delete")
    private Long id;
    @Parameter(name = ApiConstants.CLEANUP,
            type = CommandType.BOOLEAN,
            description = "true if all domain resources (child domains, accounts) have to be cleaned up, false otherwise")
    private Boolean cleanup;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Boolean getCleanup() {
        return cleanup;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_DOMAIN_DELETE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "deleting domain: " + getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Domain Id: " + getId());
        final boolean result = _regionService.deleteDomain(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete domain");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Domain domain = _entityMgr.findById(Domain.class, getId());
        if (domain != null) {
            return domain.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
