package org.apache.cloudstack.api.command.user.loadbalancer;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.network.rules.StickinessPolicy;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.LBStickinessResponse;
import org.apache.cloudstack.context.CallContext;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createLBStickinessPolicy", description = "Creates a load balancer stickiness policy ", responseObject = LBStickinessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateLBStickinessPolicyCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateLBStickinessPolicyCmd.class.getName());

    private static final String s_name = "createLBStickinessPolicy";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.LBID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the load balancer rule")
    private Long lbRuleId;

    @Parameter(name = ApiConstants.DESCRIPTION, type = CommandType.STRING, description = "the description of the load balancer stickiness policy")
    private String description;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "name of the load balancer stickiness policy")
    private String lbStickinessPolicyName;

    @Parameter(name = ApiConstants.METHOD_NAME,
            type = CommandType.STRING,
            required = true,
            description = "name of the load balancer stickiness policy method, possible values can be obtained from listNetworks API")
    private String stickinessMethodName;

    @Parameter(name = ApiConstants.PARAM_LIST, type = CommandType.MAP, description = "param list. Example: param[0].name=cookiename&param[0].value=LBCookie ")
    private Map paramList;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the rule to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Deprecated
    public Boolean getDisplay() {
        return display;
    }

    public String getDescription() {
        return description;
    }

    public String getStickinessMethodName() {
        return stickinessMethodName;
    }

    public Map getparamList() {
        return paramList;
    }

    @Override
    public void execute() throws ResourceAllocationException, ResourceUnavailableException {
        StickinessPolicy policy = null;
        boolean success = false;

        try {
            CallContext.current().setEventDetails("Rule Id: " + getEntityId());
            success = _lbService.applyLBStickinessPolicy(this);
            if (success) {
                // State might be different after the rule is applied, so get new object here
                policy = _entityMgr.findById(StickinessPolicy.class, getEntityId());
                final LoadBalancer lb = _lbService.findById(policy.getLoadBalancerId());
                final LBStickinessResponse spResponse = _responseGenerator.createLBStickinessPolicyResponse(policy, lb);
                setResponseObject(spResponse);
                spResponse.setResponseName(getCommandName());
            }
        } finally {
            if (!success || (policy == null)) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create stickiness policy");
            }
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account account = CallContext.current().getCallingAccount();
        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public boolean isDisplay() {
        if (display == null) {
            return true;
        } else {
            return display;
        }
    }

    @Override
    public void create() {
        try {
            final StickinessPolicy result = _lbService.createLBStickinessPolicy(this);
            this.setEntityId(result.getId());
            this.setEntityUuid(result.getUuid());
        } catch (final NetworkRuleConflictException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LB_STICKINESSPOLICY_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating a load balancer stickiness policy: " + getLBStickinessPolicyName();
    }

    public String getLBStickinessPolicyName() {
        return lbStickinessPolicyName;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final LoadBalancer lb = _lbService.findById(getLbRuleId());
        if (lb == null) {
            throw new InvalidParameterValueException("Unable to find load balancer rule " + getLbRuleId() + " to create stickiness rule");
        }
        return lb.getNetworkId();
    }

    public Long getLbRuleId() {
        return lbRuleId;
    }
}
