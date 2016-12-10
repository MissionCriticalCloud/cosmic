package com.cloud.api.command.user.ratelimit;

import com.cloud.api.APICommand;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ApiLimitResponse;
import com.cloud.configuration.Config;
import com.cloud.context.CallContext;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.ratelimit.ApiRateLimitService;
import com.cloud.user.Account;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "getApiLimit", responseObject = ApiLimitResponse.class, description = "Get API limit count for the caller",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class GetApiLimitCmd extends BaseCmd {
    private static final Logger s_logger = LoggerFactory.getLogger(GetApiLimitCmd.class.getName());

    private static final String s_name = "getapilimitresponse";

    @Inject
    ApiRateLimitService _apiLimitService;

    @Inject
    ConfigurationDao _configDao;

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean apiLimitEnabled = Boolean.parseBoolean(_configDao.getValue(Config.ApiLimitEnabled.key()));
        if (!apiLimitEnabled) {
            throw new ServerApiException(ApiErrorCode.UNSUPPORTED_ACTION_ERROR, "This api is only available when api.throttling.enabled = true.");
        }
        final Account caller = CallContext.current().getCallingAccount();
        final ApiLimitResponse response = _apiLimitService.searchApiLimit(caller);
        response.setResponseName(getCommandName());
        response.setObjectName("apilimit");
        this.setResponseObject(response);
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
