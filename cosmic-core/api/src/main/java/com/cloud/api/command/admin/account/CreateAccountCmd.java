package com.cloud.api.command.admin.account;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.context.CallContext;
import com.cloud.user.Account;
import com.cloud.user.UserAccount;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createAccount", description = "Creates an account", responseObject = AccountResponse.class, entityType = {Account.class},
        requestHasSensitiveInfo = true, responseHasSensitiveInfo = true)
public class CreateAccountCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateAccountCmd.class.getName());

    private static final String s_name = "createaccountresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT,
            type = CommandType.STRING,
            description = "Creates the user under the specified account. If no account is specified, the username will be used as the account name.")
    private String accountName;

    @Parameter(name = ApiConstants.ACCOUNT_TYPE,
            type = CommandType.SHORT,
            required = true,
            description = "Type of the account.  Specify 0 for user, 1 for root admin, and 2 for domain admin")
    private Short accountType;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "Creates the user under the specified domain.")
    private Long domainId;

    @Parameter(name = ApiConstants.EMAIL, type = CommandType.STRING, required = true, description = "email")
    private String email;

    @Parameter(name = ApiConstants.FIRSTNAME, type = CommandType.STRING, required = true, description = "firstname")
    private String firstName;

    @Parameter(name = ApiConstants.LASTNAME, type = CommandType.STRING, required = true, description = "lastname")
    private String lastName;

    @Parameter(name = ApiConstants.PASSWORD,
            type = CommandType.STRING,
            required = true,
            description = "Clear text password (Default hashed to SHA256SALT). If you wish to use any other hashing algorithm, you would need to write a custom authentication " +
                    "adapter See Docs section.")
    private String password;

    @Parameter(name = ApiConstants.TIMEZONE,
            type = CommandType.STRING,
            description = "Specifies a timezone for this command. For more information on the timezone parameter, see Time Zone Format.")
    private String timeZone;

    @Parameter(name = ApiConstants.USERNAME, type = CommandType.STRING, required = true, description = "Unique username.")
    private String userName;

    @Parameter(name = ApiConstants.NETWORK_DOMAIN, type = CommandType.STRING, description = "Network domain for the account's networks")
    private String networkDomain;

    @Parameter(name = ApiConstants.ACCOUNT_DETAILS, type = CommandType.MAP, description = "details for account used to store specific parameters")
    private Map<String, String> details;

    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.STRING, description = "Account UUID, required for adding account from external provisioning system")
    private String accountUUID;

    @Parameter(name = ApiConstants.USER_ID, type = CommandType.STRING, description = "User UUID, required for adding account from external provisioning system")
    private String userUUID;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        validateParams();
        CallContext.current().setEventDetails("Account Name: " + getAccountName() + ", Domain Id:" + getDomainId());
        final UserAccount userAccount =
                _accountService.createUserAccount(getUsername(), getPassword(), getFirstName(), getLastName(), getEmail(), getTimeZone(), getAccountName(), getAccountType(),
                        getDomainId(), getNetworkDomain(), getDetails(), getAccountUUID(), getUserUUID());
        if (userAccount != null) {
            final AccountResponse response = _responseGenerator.createUserAccountResponse(ResponseView.Full, userAccount);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create a user account");
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

    /**
     * TODO: this should be done through a validator. for now replicating the validation logic in create account and user
     */
    private void validateParams() {
        if (StringUtils.isEmpty(getPassword())) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "Empty passwords are not allowed");
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getUsername() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public Short getAccountType() {
        return accountType;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getNetworkDomain() {
        return networkDomain;
    }

    public Map<String, String> getDetails() {
        if (details == null || details.isEmpty()) {
            return null;
        }

        final Collection<String> paramsCollection = details.values();
        final Map<String, String> params = (Map<String, String>) (paramsCollection.toArray())[0];
        return params;
    }

    public String getAccountUUID() {
        return accountUUID;
    }

    public String getUserUUID() {
        return userUUID;
    }
}
