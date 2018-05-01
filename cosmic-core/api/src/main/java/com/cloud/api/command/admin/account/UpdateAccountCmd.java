package com.cloud.api.command.admin.account;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.legacymodel.user.Account;
import com.cloud.region.RegionService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateAccount", group = APICommandGroup.AccountService, description = "Updates account information for the authenticated user", responseObject = AccountResponse.class,
        entityType = {Account.class},
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
