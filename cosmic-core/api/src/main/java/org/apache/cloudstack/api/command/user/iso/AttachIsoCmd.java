package org.apache.cloudstack.api.command.user.iso;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.uservm.UserVm;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.vm.DeployVMCmd;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "attachIso", description = "Attaches an ISO to a virtual machine.", responseObject = UserVmResponse.class, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class AttachIsoCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AttachIsoCmd.class.getName());

    private static final String s_name = "attachisoresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TemplateResponse.class,
            required = true, description = "the ID of the ISO file")
    protected Long id;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class,
            required = true, description = "the ID of the virtual machine")
    protected Long virtualMachineId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ISO_ATTACH;
    }

    @Override
    public String getEventDescription() {
        return "attaching ISO: " + getId() + " to VM: " + getVirtualMachineId();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Vm Id: " + getVirtualMachineId() + " ISO ID: " + getId());
        final boolean result = _templateService.attachIso(id, virtualMachineId);
        if (result) {
            final UserVm userVm = _responseGenerator.findUserVmById(virtualMachineId);
            if (userVm != null) {
                final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", userVm).get(0);
                response.setResponseName(DeployVMCmd.getResultObjectName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to attach ISO");
            }
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to attach ISO");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm vm = _entityMgr.findById(UserVm.class, getVirtualMachineId());
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find virtual machine by ID " + getVirtualMachineId());
        }

        return vm.getAccountId();
    }
}
