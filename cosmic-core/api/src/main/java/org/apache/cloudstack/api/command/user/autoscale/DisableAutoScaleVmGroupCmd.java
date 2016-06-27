package org.apache.cloudstack.api.command.user.autoscale;

import com.cloud.event.EventTypes;
import com.cloud.network.as.AutoScaleVmGroup;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AutoScaleVmGroupResponse;

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
