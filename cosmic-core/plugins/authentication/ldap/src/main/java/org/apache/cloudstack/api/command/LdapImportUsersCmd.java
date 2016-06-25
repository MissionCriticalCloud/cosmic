package org.apache.cloudstack.api.command;

import com.cloud.domain.Domain;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.user.AccountService;
import com.cloud.user.DomainService;
import com.cloud.user.User;
import com.cloud.user.UserAccount;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.LdapUserResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.ldap.LdapManager;
import org.apache.cloudstack.ldap.LdapUser;
import org.apache.cloudstack.ldap.NoLdapUserMatchingQueryException;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "importLdapUsers", description = "Import LDAP users", responseObject = LdapUserResponse.class, since = "4.3.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class LdapImportUsersCmd extends BaseListCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(LdapImportUsersCmd.class.getName());

    private static final String s_name = "ldapuserresponse";

    @Parameter(name = ApiConstants.TIMEZONE,
            type = CommandType.STRING,
            description = "Specifies a timezone for this command. For more information on the timezone parameter, see Time Zone Format.")
    private String timezone;

    @Parameter(name = ApiConstants.ACCOUNT_TYPE,
            type = CommandType.SHORT,
            required = true,
            description = "Type of the account.  Specify 0 for user, 1 for root admin, and 2 for domain admin")
    private Short accountType;

    @Parameter(name = ApiConstants.ACCOUNT_DETAILS, type = CommandType.MAP, description = "details for account used to store specific parameters")
    private Map<String, String> details;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "Specifies the domain to which the ldap users are to be "
                    + "imported. If no domain is specified, a domain will created using group parameter. If the group is also not specified, a domain name based on the OU " +
                    "information will be "
                    + "created. If no OU hierarchy exists, will be defaulted to ROOT domain")
    private Long domainId;

    @Parameter(name = ApiConstants.GROUP, type = CommandType.STRING, description = "Specifies the group name from which the ldap users are to be imported. "
            + "If no group is specified, all the users will be imported.")
    private String groupName;

    private Domain _domain;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "Creates the user under the specified account. If no account is specified, the username will" +
            " be used as the account name.")
    private String accountName;

    @Inject
    private LdapManager _ldapManager;

    public LdapImportUsersCmd() {
        super();
    }

    public LdapImportUsersCmd(final LdapManager ldapManager, final DomainService domainService, final AccountService accountService) {
        super();
        _ldapManager = ldapManager;
        _domainService = domainService;
        _accountService = accountService;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException, NetworkRuleConflictException {

        List<LdapUser> users;
        try {
            if (StringUtils.isNotBlank(groupName)) {

                users = _ldapManager.getUsersInGroup(groupName);
            } else {
                users = _ldapManager.getUsers();
            }
        } catch (final NoLdapUserMatchingQueryException ex) {
            users = new ArrayList<>();
            s_logger.info("No Ldap user matching query. " + " ::: " + ex.getMessage());
        }

        final List<LdapUser> addedUsers = new ArrayList<>();
        for (final LdapUser user : users) {
            final Domain domain = getDomain(user);
            try {
                createCloudstackUserAccount(user, getAccountName(user), domain);
                addedUsers.add(user);
            } catch (final InvalidParameterValueException ex) {
                s_logger.error("Failed to create user with username: " + user.getUsername() + " ::: " + ex.getMessage());
            }
        }
        final ListResponse<LdapUserResponse> response = new ListResponse<>();
        response.setResponses(createLdapUserResponse(addedUsers));
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    private Domain getDomain(final LdapUser user) {
        Domain domain;
        if (_domain != null) {
            //this means either domain id or groupname is passed and this will be same for all the users in this call. hence returning it.
            domain = _domain;
        } else {
            if (domainId != null) {
                // a domain Id is passed. use it for this user and all the users in the same api call (by setting _domain)
                domain = _domain = _domainService.getDomain(domainId);
            } else {
                // a group name is passed. use it for this user and all the users in the same api call(by setting _domain)
                domain = _domain = getDomainForName(groupName);
                if (domain == null) {
                    //use the domain from the LDAP for this user
                    domain = getDomainForName(user.getDomain());
                }
            }
            if (domain == null) {
                // could not get a domain using domainId / LDAP group / OU of LDAP user. using ROOT domain for this user
                domain = _domainService.getDomain(Domain.ROOT_DOMAIN);
            }
        }
        return domain;
    }

    private void createCloudstackUserAccount(final LdapUser user, final String accountName, final Domain domain) {
        final Account account = _accountService.getActiveAccountByName(accountName, domain.getId());
        if (account == null) {
            s_logger.debug("No account exists with name: " + accountName + " creating the account and an user with name: " + user.getUsername() + " in the account");
            _accountService.createUserAccount(user.getUsername(), generatePassword(), user.getFirstname(), user.getLastname(), user.getEmail(), timezone, accountName, accountType,
                    domain.getId(), domain.getNetworkDomain(), details, UUID.randomUUID().toString(), UUID.randomUUID().toString(), User.Source.LDAP);
        } else {
            //            check if the user exists. if yes, call update
            final UserAccount csuser = _accountService.getActiveUserAccount(user.getUsername(), domain.getId());
            if (csuser == null) {
                s_logger.debug("No user exists with name: " + user.getUsername() + " creating a user in the account: " + accountName);
                _accountService.createUser(user.getUsername(), generatePassword(), user.getFirstname(), user.getLastname(), user.getEmail(), timezone, accountName, domain.getId(),
                        UUID.randomUUID().toString(), User.Source.LDAP);
            } else {
                s_logger.debug("account with name: " + accountName + " exist and user with name: " + user.getUsername() + " exists in the account. Updating the account.");
                _accountService.updateUser(csuser.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), null, null, null, null, null);
            }
        }
    }

    private String getAccountName(final LdapUser user) {
        String finalAccountName = accountName;
        if (finalAccountName == null) {
            finalAccountName = user.getUsername();
        }
        return finalAccountName;
    }

    private List<LdapUserResponse> createLdapUserResponse(final List<LdapUser> users) {
        final List<LdapUserResponse> ldapResponses = new ArrayList<>();
        for (final LdapUser user : users) {
            final LdapUserResponse ldapResponse = _ldapManager.createLdapUserResponse(user);
            ldapResponse.setObjectName("LdapUser");
            ldapResponses.add(ldapResponse);
        }
        return ldapResponses;
    }

    private Domain getDomainForName(final String name) {
        Domain domain = null;
        if (StringUtils.isNotBlank(name)) {
            //removing all the special characters and trimming its length to 190 to make the domain valid.
            final String domainName = StringUtils.substring(name.replaceAll("\\W", ""), 0, 190);
            if (StringUtils.isNotBlank(domainName)) {
                domain = _domainService.getDomainByName(domainName, Domain.ROOT_DOMAIN);
                if (domain == null) {
                    domain = _domainService.createDomain(domainName, Domain.ROOT_DOMAIN, domainName, UUID.randomUUID().toString());
                }
            }
        }
        return domain;
    }

    private String generatePassword() throws ServerApiException {
        try {
            final SecureRandom randomGen = SecureRandom.getInstance("SHA1PRNG");
            final byte bytes[] = new byte[20];
            randomGen.nextBytes(bytes);
            return new String(Base64.encode(bytes), "UTF-8");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to generate random password");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
