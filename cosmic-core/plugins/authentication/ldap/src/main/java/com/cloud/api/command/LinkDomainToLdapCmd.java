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
import com.cloud.ldap.LdapUser;
import com.cloud.ldap.NoLdapUserMatchingQueryException;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.user.UserAccount;
import com.cloud.utils.exception.InvalidParameterValueException;

import javax.inject.Inject;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "linkDomainToLdap", description = "link an existing cloudstack domain to group or OU in ldap", responseObject = LinkDomainToLdapResponse.class, since = "4.6.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class LinkDomainToLdapCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(LinkDomainToLdapCmd.class.getName());
    private static final String s_name = "linkdomaintoldapresponse";

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, required = true, entityType = DomainResponse.class, description = "The id of the domain which has to be "
            + "linked to LDAP.")
    private Long domainId;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, required = true, description = "type of the ldap name. GROUP or OU")
    private String type;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "name of the group or OU in LDAP")
    private String name;

    @Parameter(name = ApiConstants.ADMIN, type = CommandType.STRING, required = false, description = "domain admin username in LDAP ")
    private String admin;

    @Parameter(name = ApiConstants.ACCOUNT_TYPE, type = CommandType.SHORT, required = true, description = "Type of the account to auto import. Specify 0 for user and 2 for " +
            "domain admin")
    private short accountType;

    @Inject
    private LdapManager _ldapManager;

    @Override
    public void execute() throws ServerApiException {
        try {
            final LinkDomainToLdapResponse response = _ldapManager.linkDomainToLdap(domainId, type, name, accountType);
            if (admin != null) {
                LdapUser ldapUser = null;
                try {
                    ldapUser = _ldapManager.getUser(admin, type, name);
                } catch (final NoLdapUserMatchingQueryException e) {
                    s_logger.debug("no ldap user matching username " + admin + " in the given group/ou", e);
                }
                if (ldapUser != null && !ldapUser.isDisabled()) {
                    final Account account = _accountService.getActiveAccountByName(admin, domainId);
                    if (account == null) {
                        try {
                            final UserAccount userAccount = _accountService.createUserAccount(admin, "", ldapUser.getFirstname(), ldapUser.getLastname(), ldapUser.getEmail(), null,
                                    admin, Account.ACCOUNT_TYPE_DOMAIN_ADMIN, domainId, null, null, UUID.randomUUID().toString(), UUID.randomUUID().toString(), User.Source.LDAP);
                            response.setAdminId(String.valueOf(userAccount.getAccountId()));
                            s_logger.info("created an account with name " + admin + " in the given domain " + domainId);
                        } catch (final Exception e) {
                            s_logger.info("an exception occurred while creating account with name " + admin + " in domain " + domainId, e);
                        }
                    } else {
                        s_logger.debug("an account with name " + admin + " already exists in the domain " + domainId);
                    }
                } else {
                    s_logger.debug("ldap user with username " + admin + " is disabled in the given group/ou");
                }
            }
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
}
