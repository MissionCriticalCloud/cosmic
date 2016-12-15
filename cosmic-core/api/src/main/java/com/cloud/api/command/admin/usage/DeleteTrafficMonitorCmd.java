package com.cloud.api.command.admin.usage;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.user.Account;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteTrafficMonitor", description = "Deletes an traffic monitor host.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteTrafficMonitorCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteTrafficMonitorCmd.class.getName());
    private static final String s_name = "deletetrafficmonitorresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = HostResponse.class, required = true, description = "Id of the Traffic Monitor Host.")
    private Long id;

    ///////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        try {
            final boolean result = _networkUsageService.deleteTrafficMonitor(this);
            if (result) {
                final SuccessResponse response = new SuccessResponse(getCommandName());
                response.setResponseName(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete traffic monitor.");
            }
        } catch (final InvalidParameterValueException e) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "Failed to delete traffic monitor.");
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
}
