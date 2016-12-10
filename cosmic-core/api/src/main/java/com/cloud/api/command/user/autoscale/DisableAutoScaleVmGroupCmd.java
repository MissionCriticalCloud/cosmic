package com.cloud.api.command.user.autoscale;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AutoScaleVmGroupResponse;
import com.cloud.event.EventTypes;
import com.cloud.network.as.AutoScaleVmGroup;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "disableAutoScaleVmGroup", description = "Disables an AutoScale Vm Group", responseObject = AutoScaleVmGroupResponse.class, entityType = {AutoScaleVmGroup
        .class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DisableAutoScaleVmGroupCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DisableAutoScaleVmGroupCmd.class.getName());
    private static final String s_name = "disableautoscalevmGroupresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = AutoScaleVmGroupResponse.class,
            required = true,
            description = "the ID of the autoscale group")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final AutoScaleVmGroup result = _autoScaleService.disableAutoScaleVmGroup(getId());
        if (result != null) {
            final AutoScaleVmGroupResponse response = _responseGenerator.createAutoScaleVmGroupResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to disable AutoScale Vm Group");
        }
    }

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final AutoScaleVmGroup autoScaleVmGroup = _entityMgr.findById(AutoScaleVmGroup.class, getId());
        if (autoScaleVmGroup != null) {
            return autoScaleVmGroup.getAccountId();
        }
        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are
        // tracked
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_AUTOSCALEVMGROUP_DISABLE;
    }

    @Override
    public String getEventDescription() {
        return "Disabling AutoScale Vm Group. Vm Group Id: " + getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.AutoScaleVmGroup;
    }
}
