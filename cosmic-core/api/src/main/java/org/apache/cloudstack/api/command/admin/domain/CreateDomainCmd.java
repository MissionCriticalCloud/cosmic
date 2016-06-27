package org.apache.cloudstack.api.command.admin.domain;

import com.cloud.domain.Domain;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createDomain", description = "Creates a domain", responseObject = DomainResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateDomainCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateDomainCmd.class.getName());

    private static final String s_name = "createdomainresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "creates domain with this name")
    private String domainName;

    @Parameter(name = ApiConstants.PARENT_DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "assigns new domain a parent domain by domain ID of the parent.  If no parent domain is specied, the ROOT domain is assumed.")
    private Long parentDomainId;

    @Parameter(name = ApiConstants.NETWORK_DOMAIN, type = CommandType.STRING, description = "Network domain for networks in the domain")
    private String networkDomain;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.STRING, description = "Domain UUID, required for adding domain from another Region")
    private String domainUUID;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Domain Name: " + getDomainName() + ((getParentDomainId() != null) ? ", Parent DomainId :" + getParentDomainId() : ""));
        final Domain domain = _domainService.createDomain(getDomainName(), getParentDomainId(), getNetworkDomain(), getDomainUUID());
        if (domain != null) {
            final DomainResponse response = _responseGenerator.createDomainResponse(domain);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create domain");
        }
    }

    public String getDomainName() {
        return domainName;
    }

    public Long getParentDomainId() {
        return parentDomainId;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getDomainUUID() {
        return domainUUID;
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
