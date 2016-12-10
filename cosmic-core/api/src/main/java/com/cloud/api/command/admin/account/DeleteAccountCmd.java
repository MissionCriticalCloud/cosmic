package com.cloud.api.command.admin.account;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.region.RegionService;
import com.cloud.user.Account;
import com.cloud.user.User;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteAccount", description = "Deletes a account, and all users associated with this account", responseObject = SuccessResponse.class, entityType = {Account
        .class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteAccountCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteAccountCmd.class.getName());
    private static final String s_name = "deleteaccountresponse";
    @Inject
    RegionService _regionService;
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = AccountResponse.class, required = true, description = "Account id")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getStaticName() {
        return s_name;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ACCOUNT_DELETE;
    }

    @Override
    public String getEventDescription() {
        final User user = _responseGenerator.findUserById(getId());
        return (user != null ? ("deleting User " + user.getUsername() + " (id: " + user.getId() + ") and accountId = " + user.getAccountId())
                : "user delete, but this user does not exist in the system");
    }

    public Long getId() {
        return id;
    }

    @Override
    public Long getInstanceId() {
        return id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Account;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Account Id: " + getId());

        final boolean result = _regionService.deleteUserAccount(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete user account and all corresponding users");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account account = CallContext.current().getCallingAccount();// Let's give the caller here for event logging.
        if (account != null) {
            return account.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
