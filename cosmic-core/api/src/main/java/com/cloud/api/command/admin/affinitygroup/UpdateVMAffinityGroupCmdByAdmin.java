package com.cloud.api.command.admin.affinitygroup;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.affinitygroup.UpdateVMAffinityGroupCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVMAffinityGroup", group = APICommandGroup.AffinityGroupService, description = "Updates the affinity/anti-affinity group associations of a virtual machine. The VM has to be stopped and restarted for" +
        " the "
        + "new properties to take effect.", responseObject = UserVmResponse.class, responseView = ResponseView.Full,
        entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class UpdateVMAffinityGroupCmdByAdmin extends UpdateVMAffinityGroupCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVMAffinityGroupCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceUnavailableException,
            InsufficientCapacityException, ServerApiException {
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final UserVm result = _affinityGroupService.updateVMAffinityGroups(getId(), getAffinityGroupIdList());
        final ArrayList<VMDetails> dc = new ArrayList<>();
        dc.add(VMDetails.valueOf("affgrp"));
        final EnumSet<VMDetails> details = EnumSet.copyOf(dc);

        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", details, result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update vm's affinity groups");
        }
    }
}
