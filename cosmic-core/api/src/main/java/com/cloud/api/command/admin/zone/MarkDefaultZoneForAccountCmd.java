package com.cloud.api.command.admin.zone;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "markDefaultZoneForAccount", group = APICommandGroup.AccountService, description = "Marks a default zone for this account", responseObject = AccountResponse.class, since = "4.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class MarkDefaultZoneForAccountCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(MarkDefaultZoneForAccountCmd.class.getName());

    private static final String s_name = "markdefaultzoneforaccountresponse";

    /////////////////////////////////////////////////////
    ////////////////API parameters //////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT,
            type = CommandType.STRING,
            entityType = AccountResponse.class,
            required = true,
            description = "Name of the account that is to be marked.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            required = true,
            description = "Marks the account that belongs to the specified domain.")
    private Long domainId;

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            required = true,
            description = "The Zone ID with which the account is to be marked.")
    private Long defaultZoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ACCOUNT_MARK_DEFAULT_ZONE;
    }

    @Override
    public String getEventDescription() {
        return "Marking account with the default zone: " + getDefaultZoneId();
    }

    public Long getDefaultZoneId() {
        return defaultZoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Account;
    }

    @Override
    public void execute() {
        final Account result = _configService.markDefaultZone(getAccountName(), getDomainId(), getDefaultZoneId());
        if (result != null) {
            final AccountResponse response = _responseGenerator.createAccountResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to mark the account with the default zone");
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
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
