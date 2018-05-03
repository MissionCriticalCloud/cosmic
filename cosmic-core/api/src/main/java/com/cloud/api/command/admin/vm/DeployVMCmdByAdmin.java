package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.DeployVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientServerCapacityException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.uservm.UserVm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deployVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Creates and automatically starts a virtual machine based on a service offering, disk " +
        "offering, and template.",
        responseObject = UserVmResponse.class, responseView = ResponseView.Full, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class DeployVMCmdByAdmin extends DeployVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeployVMCmdByAdmin.class.getName());

    @Override
    public void execute() {
        final UserVm result;

        if (getStartVm()) {
            try {
                CallContext.current().setEventDetails("Vm Id: " + getEntityId());
                result = _userVmService.startVirtualMachine(this);
            } catch (final ResourceUnavailableException ex) {
                s_logger.warn("Exception: ", ex);
                throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
            } catch (final ConcurrentOperationException ex) {
                s_logger.warn("Exception: ", ex);
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
            } catch (final InsufficientCapacityException ex) {
                final StringBuilder message = new StringBuilder(ex.getMessage());
                if (ex instanceof InsufficientServerCapacityException) {
                    if (((InsufficientServerCapacityException) ex).isAffinityApplied()) {
                        message.append(", Please check the affinity groups provided, there may not be sufficient capacity to follow them");
                    }
                }
                s_logger.info(ex.toString());
                s_logger.info(message.toString(), ex);
                throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, message.toString());
            }
        } else {
            result = _userVmService.getUserVm(getEntityId());
        }

        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to deploy vm");
        }
    }
}
