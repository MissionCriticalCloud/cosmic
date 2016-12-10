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
import com.cloud.api.response.ConditionResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.network.as.AutoScalePolicy;
import com.cloud.user.Account;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateAutoScalePolicy", description = "Updates an existing autoscale policy.", responseObject = AutoScalePolicyResponse.class, entityType = {AutoScalePolicy
        .class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateAutoScalePolicyCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateAutoScalePolicyCmd.class.getName());

    private static final String s_name = "updateautoscalepolicyresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.DURATION, type = CommandType.INTEGER, description = "the duration for which the conditions have to be true before action is taken")
    private Integer duration;

    @Parameter(name = ApiConstants.QUIETTIME,
            type = CommandType.INTEGER,
            description = "the cool down period for which the policy should not be evaluated after the action has been taken")
    private Integer quietTime;

    @Parameter(name = ApiConstants.CONDITION_IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = ConditionResponse.class,
            description = "the list of IDs of the conditions that are being evaluated on every interval")
    private List<Long> conditionIds;

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = AutoScalePolicyResponse.class,
            required = true,
            description = "the ID of the autoscale policy")
    private Long id;

    @Override
    public void execute() {
        CallContext.current().setEventDetails("AutoScale Policy Id: " + getId());
        final AutoScalePolicy result = _autoScaleService.updateAutoScalePolicy(this);
        if (result != null) {
            final AutoScalePolicyResponse response = _responseGenerator.createAutoScalePolicyResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update autoscale policy");
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
        final AutoScalePolicy autoScalePolicy = _entityMgr.findById(AutoScalePolicy.class, getId());
        if (autoScalePolicy != null) {
            return autoScalePolicy.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are
        // tracked
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getQuietTime() {
        return quietTime;
    }

    public List<Long> getConditionIds() {
        return conditionIds;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_AUTOSCALEPOLICY_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating Auto Scale Policy. Policy Id: " + getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.AutoScalePolicy;
    }
}
