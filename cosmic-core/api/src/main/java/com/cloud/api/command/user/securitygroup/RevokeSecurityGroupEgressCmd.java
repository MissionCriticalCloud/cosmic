package com.cloud.api.command.user.securitygroup;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SecurityGroupRuleResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.event.EventTypes;
import com.cloud.network.security.SecurityGroup;
import com.cloud.network.security.SecurityRule;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "revokeSecurityGroupEgress", responseObject = SuccessResponse.class, description = "Deletes a particular egress rule from this security group", since = "3.0" +
        ".0", entityType = {SecurityGroup.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class RevokeSecurityGroupEgressCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RevokeSecurityGroupEgressCmd.class.getName());

    private static final String s_name = "revokesecuritygroupegressresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, required = true, description = "The ID of the egress rule", entityType = SecurityGroupRuleResponse.class)
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "revokesecuritygroupegress";
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SECURITY_GROUP_REVOKE_EGRESS;
    }

    @Override
    public String getEventDescription() {
        return "revoking egress rule id: " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.SecurityGroup;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        final boolean result = _securityGroupService.revokeSecurityGroupEgress(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to revoke security group egress rule");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final SecurityRule rule = _entityMgr.findById(SecurityRule.class, getId());
        if (rule != null) {
            final SecurityGroup group = _entityMgr.findById(SecurityGroup.class, rule.getSecurityGroupId());
            if (group != null) {
                return group.getAccountId();
            }
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
