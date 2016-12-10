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
import com.cloud.api.response.AutoScalePolicyResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.network.as.AutoScalePolicy;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteAutoScalePolicy", description = "Deletes a autoscale policy.", responseObject = SuccessResponse.class, entityType = {AutoScalePolicy.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteAutoScalePolicyCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteAutoScalePolicyCmd.class.getName());
    private static final String s_name = "deleteautoscalepolicyresponse";
    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = AutoScalePolicyResponse.class,
            required = true,
            description = "the ID of the autoscale policy")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_AUTOSCALEPOLICY_DELETE;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "deleting AutoScale Policy: " + getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.AutoScalePolicy;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("AutoScale Policy Id: " + getId());
        final boolean result = _autoScaleService.deleteAutoScalePolicy(id);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            s_logger.warn("Failed to delete autoscale policy " + getId());
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete AutoScale Policy");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final AutoScalePolicy autoScalePolicy = _entityMgr.findById(AutoScalePolicy.class, getId());
        if (autoScalePolicy != null) {
            return autoScalePolicy.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are
        // tracked
    }
}
