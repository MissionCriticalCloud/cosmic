package org.apache.cloudstack.api.command.admin.account;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.region.RegionService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "disableAccount", description = "Disables an account", responseObject = AccountResponse.class, entityType = {Account.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class DisableAccountCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DisableAccountCmd.class.getName());
    private static final String s_name = "disableaccountresponse";
    @Inject
    RegionService _regionService;
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = AccountResponse.class, description = "Account id")
    private Long id;
    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "Disables specified account.")
    private String accountName;
    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "Disables specified account in this domain.")
    private Long domainId;
    @Parameter(name = ApiConstants.LOCK, type = CommandType.BOOLEAN, required = true, description = "If true, only lock the account; else disable the account")
    private Boolean lockRequested;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Boolean getLockRequested() {
        return lockRequested;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ACCOUNT_DISABLE;
    }

    @Override
    public String getEventDescription() {
        return "disabling account: " + getAccountName() + " in domain: " + getDomainId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Account;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    @Override
    public void execute() throws ConcurrentOperationException, ResourceUnavailableException {
        CallContext.current().setEventDetails("Account Name: " + getAccountName() + ", Domain Id:" + getDomainId());
        final Account result = _regionService.disableAccount(this);
        if (result != null) {
            final AccountResponse response = _responseGenerator.createAccountResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, lockRequested == true ? "Failed to lock account" : "Failed to disable account");
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

    public Long getId() {
        return id;
    }
}
