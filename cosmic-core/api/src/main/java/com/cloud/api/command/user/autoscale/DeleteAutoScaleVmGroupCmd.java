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
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.network.as.AutoScaleVmGroup;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteAutoScaleVmGroup", description = "Deletes a autoscale vm group.", responseObject = SuccessResponse.class, entityType = {AutoScaleVmGroup.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteAutoScaleVmGroupCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteAutoScaleVmGroupCmd.class.getName());
    private static final String s_name = "deleteautoscalevmgroupresponse";
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
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_AUTOSCALEVMGROUP_DELETE;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "deleting autoscale vm group: " + getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.AutoScaleVmGroup;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("AutoScale Vm Group Id: " + getId());
        final boolean result = _autoScaleService.deleteAutoScaleVmGroup(id);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            s_logger.warn("Failed to delete autoscale vm group " + getId());
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete autoscale vm group");
        }
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
}
