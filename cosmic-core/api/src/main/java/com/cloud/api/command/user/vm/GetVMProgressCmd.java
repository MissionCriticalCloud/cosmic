package com.cloud.api.command.user.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.VmProgressResponse;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.uservm.UserVm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "getvmprogress", group = APICommandGroup.VirtualMachineService, description = "Get migration progress of VM", responseObject = VmProgressResponse.class)
public class GetVMProgressCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(GetVMProgressCmd.class.getName());

    private static final String COMMAND_NAME = "getvmprogressresponse";
    @Parameter(name = ApiConstants.UUID, type = BaseCmd.CommandType.STRING, required = true, description = "The UUID of the VM.")
    private String uuid;

    @Override
    public void execute() {

        try {
            final VmProgressResponse response = _userVmService.getVmProgress(this);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (InvalidParameterValueException e) {
            s_logger.error("Invalid parameter: " + e.getMessage());
        } catch (CloudRuntimeException e) {
            s_logger.error("CloudRuntimeException: " + e.getMessage());
        } catch (Exception e) {
            s_logger.error("Unexpected exception: " + e.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm userVm = _entityMgr.findByUuid(UserVm.class, getUuid());
        if (userVm != null) {
            return userVm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public String getUuid() {
        return uuid;
    }
}
