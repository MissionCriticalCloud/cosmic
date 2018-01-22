package com.cloud.api.command.admin.resource;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "cleanVMReservations", group = APICommandGroup.VirtualMachineService, description = "Cleanups VM reservations in the database.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CleanVMReservationsCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CleanVMReservationsCmd.class.getName());

    private static final String s_name = "cleanvmreservationresponse";

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_CLEANUP_VM_RESERVATION;
    }

    @Override
    public String getEventDescription() {
        return "cleaning vm reservations in database";
    }

    @Override
    public void execute() {
        try {
            _mgr.cleanupVMReservations();
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } catch (final Exception ex) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to clean vm reservations");
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
