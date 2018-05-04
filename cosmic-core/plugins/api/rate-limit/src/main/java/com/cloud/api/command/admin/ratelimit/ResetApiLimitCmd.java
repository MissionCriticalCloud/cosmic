package com.cloud.api.command.admin.ratelimit;

import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.ApiLimitResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.configuration.Config;
import com.cloud.context.CallContext;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.legacymodel.user.Account;
import com.cloud.ratelimit.ApiRateLimitService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "resetApiLimit", group = APICommandGroup.LimitService, responseObject = ApiLimitResponse.class, description = "Reset api count",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ResetApiLimitCmd extends BaseCmd {
    private static final Logger s_logger = LoggerFactory.getLogger(ResetApiLimitCmd.class.getName());

    private static final String s_name = "resetapilimitresponse";

    @Inject
    ApiRateLimitService _apiLimitService;

    @Inject
    ConfigurationDao _configDao;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL
    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.UUID, entityType = AccountResponse.class, description = "the ID of the acount whose limit to be reset")
    private Long accountId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean apiLimitEnabled = Boolean.parseBoolean(_configDao.getValue(Config.ApiLimitEnabled.key()));
        if (!apiLimitEnabled) {
            throw new ServerApiException(ApiErrorCode.UNSUPPORTED_ACTION_ERROR, "This api is only available when api.throttling.enabled = true.");
        }
        final boolean result = _apiLimitService.resetApiLimit(this.accountId);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to reset api limit counter");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account account = CallContext.current().getCallingAccount();
        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
