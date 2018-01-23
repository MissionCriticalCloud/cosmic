package com.cloud.api.command.admin.host;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.HostResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.host.Host;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "cancelHostMaintenance", group = APICommandGroup.HostService, description = "Cancels host maintenance.", responseObject = HostResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CancelMaintenanceCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CancelMaintenanceCmd.class.getName());

    private static final String s_name = "cancelhostmaintenanceresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = HostResponse.class, required = true, description = "the host ID")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "host";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_MAINTENANCE_CANCEL;
    }

    @Override
    public String getEventDescription() {
        return "canceling maintenance for host: " + getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Host;
    }

    @Override
    public void execute() {
        final Host result = _resourceService.cancelMaintenance(this);
        if (result != null) {
            final HostResponse response = _responseGenerator.createHostResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to cancel host maintenance");
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

        return Account.ACCOUNT_ID_SYSTEM;
    }
}
