package org.apache.cloudstack.api.command.admin.account;

import com.cloud.user.Account;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.region.RegionService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateAccount", description = "Updates account information for the authenticated user", responseObject = AccountResponse.class, entityType = {Account.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class UpdateAccountCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateAccountCmd.class.getName());
    private static final String s_name = "updateaccountresponse";
    @Inject
    RegionService _regionService;
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = AccountResponse.class, description = "Account id")
    private Long id;
    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "the current account name")
    private String accountName;
    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "the ID of the domain where the account exists")
    private Long domainId;
    @Parameter(name = ApiConstants.NEW_NAME, type = CommandType.STRING, required = true, description = "new name for the account")
    private String newName;
    @Parameter(name = ApiConstants.NETWORK_DOMAIN,
            type = CommandType.STRING,
            description = "Network domain for the account's networks; empty string will update domainName with NULL value")
    private String networkDomain;
    @Parameter(name = ApiConstants.ACCOUNT_DETAILS, type = CommandType.MAP, description = "details for account used to store specific parameters")
    private Map details;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getNewName() {
        return newName;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public Map getDetails() {
        if (details == null || details.isEmpty()) {
            return null;
        }

        final Collection paramsCollection = details.values();
        final Map params = (Map) (paramsCollection.toArray())[0];
        return params;
    }

    @Override
    public void execute() {
        final Account result = _regionService.updateAccount(this);
        if (result != null) {
            final AccountResponse response = _responseGenerator.createAccountResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update account");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        Account account = _entityMgr.findById(Account.class, getId());
        if (account != null) {
            return account.getAccountId();
        }
        account = _accountService.getActiveAccountByName(getAccountName(), getDomainId());
        if (account != null) {
            return account.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }
}
