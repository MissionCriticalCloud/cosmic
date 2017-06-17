package com.cloud.api.command;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.LinkDomainToLdapResponse;
import com.cloud.ldap.LdapManager;
import com.cloud.user.Account;
import com.cloud.utils.exception.InvalidParameterValueException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listDomainLdapLink", description = "list link of domain to group or OU in ldap", responseObject = LinkDomainToLdapResponse.class, since = "5.3.6",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListDomainLdapLinkCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListDomainLdapLinkCmd.class.getName());
    private static final String s_name = "linkdomaintoldapresponse";

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, required = true, entityType = DomainResponse.class, description = "The id of the domain which you want to "
            + "find the LDAP link for.")
    private Long domainId;

    @Inject
    private LdapManager _ldapManager;

    @Override
    public void execute() throws ServerApiException {
        try {
            final LinkDomainToLdapResponse response = _ldapManager.listLinkDomainToLdap(domainId);
            response.setObjectName("LinkDomainToLdap");
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (final InvalidParameterValueException e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.toString());
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

    public Long getDomainId() {
        return domainId;
    }
}
