package com.cloud.api.command.admin.domain;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.context.CallContext;
import com.cloud.domain.Domain;
import com.cloud.region.RegionService;
import com.cloud.user.Account;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateDomain", group = APICommandGroup.DomainService, description = "Updates a domain with a new name", responseObject = DomainResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateDomainCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateDomainCmd.class.getName());
    private static final String s_name = "updatedomainresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Inject
    RegionService _regionService;
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = DomainResponse.class, required = true, description = "ID of domain to update")
    private Long id;
    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "updates domain with this name")
    private String domainName;
    @Parameter(name = ApiConstants.NETWORK_DOMAIN,
            type = CommandType.STRING,
            description = "Network domain for the domain's networks; empty string will update domainName with NULL value")
    private String networkDomain;
    @Parameter(name = ApiConstants.EMAIL,
            type = CommandType.STRING,
            description = "Email address associated with this domain")
    private String email;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getDomainName() {
        return domainName;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Domain Id: " + getId());
        final Domain domain = _regionService.updateDomain(this);

        if (domain != null) {
            final DomainResponse response = _responseGenerator.createDomainResponse(domain);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update domain");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
