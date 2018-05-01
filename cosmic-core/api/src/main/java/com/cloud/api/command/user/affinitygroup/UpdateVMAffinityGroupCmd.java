package com.cloud.api.command.user.affinitygroup;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVMAffinityGroup", group = APICommandGroup.AffinityGroupService,
        description = "Updates the affinity/anti-affinity group associations of a virtual machine. The VM has to be stopped and restarted for the "
                + "new properties to take effect.",
        responseObject = UserVmResponse.class,
        responseView = ResponseView.Restricted,
        entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class UpdateVMAffinityGroupCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVMAffinityGroupCmd.class.getName());
    private static final String s_name = "updatevirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserVmResponse.class, required = true, description = "The ID of the virtual machine")
    private Long id;

    @ACL
    @Parameter(name = ApiConstants.AFFINITY_GROUP_IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = AffinityGroupResponse.class,
            description = "comma separated list of affinity groups id that are going to be applied to the virtual machine.")
    private List<Long> affinityGroupIdList;

    @ACL
    @Parameter(name = ApiConstants.AFFINITY_GROUP_NAMES,
            type = CommandType.LIST,
            collectionType = CommandType.STRING,
            entityType = AffinityGroupResponse.class,
            description = "comma separated list of affinity groups names that are going to be applied to the virtual machine.")
    private List<String> affinityGroupNameList;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "virtualmachine";
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException {
        CallContext.current().setEventDetails("VM ID: " + getId());
        final UserVm result = _affinityGroupService.updateVMAffinityGroups(getId(), getAffinityGroupIdList());
        final ArrayList<VMDetails> dc = new ArrayList<>();
        dc.add(VMDetails.valueOf("affgrp"));
        final EnumSet<VMDetails> details = EnumSet.copyOf(dc);

        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", details, result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update VM's affinity groups");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public List<Long> getAffinityGroupIdList() {
        if (affinityGroupNameList != null && affinityGroupIdList != null) {
            throw new InvalidParameterValueException("affinitygroupids parameter is mutually exclusive with affinitygroupnames parameter");
        }

        // transform group names to ids here
        if (affinityGroupNameList != null) {
            final List<Long> affinityGroupIds = new ArrayList<>();
            for (final String groupName : affinityGroupNameList) {
                final Long groupId = _responseGenerator.getAffinityGroupId(groupName, getEntityOwnerId());
                if (groupId == null) {
                    throw new InvalidParameterValueException("Unable to find group by name " + groupName + " for account " + getEntityOwnerId());
                } else {
                    affinityGroupIds.add(groupId);
                }
            }
            return affinityGroupIds;
        } else {
            return affinityGroupIdList;
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm userVm = _entityMgr.findById(UserVm.class, getId());
        if (userVm != null) {
            return userVm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_AFFINITY_GROUP_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "updating VM affinity group";
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.AffinityGroup;
    }
}
