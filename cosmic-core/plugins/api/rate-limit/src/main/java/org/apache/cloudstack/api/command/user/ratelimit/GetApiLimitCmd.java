package org.apache.cloudstack.api.command.user.ratelimit;

import com.cloud.configuration.Config;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.ApiLimitResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.ratelimit.ApiRateLimitService;

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
