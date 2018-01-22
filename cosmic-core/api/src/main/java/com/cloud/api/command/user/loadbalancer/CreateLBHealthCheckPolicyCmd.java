package com.cloud.api.command.user.loadbalancer;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.LBHealthCheckResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.rules.HealthCheckPolicy;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.user.Account;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createLBHealthCheckPolicy", group = APICommandGroup.LoadBalancerService,
        description = "Creates a load balancer health check policy",
        responseObject = LBHealthCheckResponse.class,
        since = "4.2.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class CreateLBHealthCheckPolicyCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateLBHealthCheckPolicyCmd.class.getName());

    private static final String s_name = "createlbhealthcheckpolicyresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.LBID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the load balancer rule")
    private Long lbRuleId;

    @Parameter(name = ApiConstants.DESCRIPTION, type = CommandType.STRING, description = "the description of the load balancer health check policy")
    private String description;

    @Parameter(name = ApiConstants.HEALTHCHECK_PINGPATH, type = CommandType.STRING, required = false, description = "HTTP ping path")
    private String pingPath;

    @Parameter(name = ApiConstants.HEALTHCHECK_RESPONSE_TIMEOUT,
            type = CommandType.INTEGER,
            required = false,
            description = "Time to wait when receiving a response from the health check (2sec - 60 sec)")
    private int responsTimeOut;

    @Parameter(name = ApiConstants.HEALTHCHECK_INTERVAL_TIME,
            type = CommandType.INTEGER,
            required = false,
            description = "Amount of time between health checks (1 sec - 20940 sec)")
    private int healthCheckInterval;

    @Parameter(name = ApiConstants.HEALTHCHECK_HEALTHY_THRESHOLD,
            type = CommandType.INTEGER,
            required = false,
            description = "Number of consecutive health check success before declaring an instance healthy")
    private int healthyThreshold;

    @Parameter(name = ApiConstants.HEALTHCHECK_UNHEALTHY_THRESHOLD,
            type = CommandType.INTEGER,
            required = false,
            description = "Number of consecutive health check failures before declaring an instance unhealthy")
    private int unhealthyThreshold;

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

    public Long getLbRuleId() {
        return lbRuleId;
    }

    public String getDescription() {
        return description;
    }

    public String getPingPath() {
        return pingPath;
    }

    public int getResponsTimeOut() {
        return responsTimeOut;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public int getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public int getHealthyThreshold() {
        return healthyThreshold;
    }

    public int getUnhealthyThreshold() {
        return unhealthyThreshold;
    }

    @Override
    public void execute() throws ResourceAllocationException, ResourceUnavailableException {
        HealthCheckPolicy policy = null;
        boolean success = false;

        try {
            CallContext.current().setEventDetails("Load balancer health check policy ID : " + getEntityId());
            success = _lbService.applyLBHealthCheckPolicy(this);
            if (success) {
                // State might be different after the rule is applied, so get new object here
                policy = _entityMgr.findById(HealthCheckPolicy.class, getEntityId());
                final LoadBalancer lb = _lbService.findById(policy.getLoadBalancerId());
                final LBHealthCheckResponse hcResponse = _responseGenerator.createLBHealthCheckPolicyResponse(policy, lb);
                setResponseObject(hcResponse);
                hcResponse.setResponseName(getCommandName());
            }
        } finally {
            if (!success || (policy == null)) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create health check policy");
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
            final HealthCheckPolicy result = _lbService.createLBHealthCheckPolicy(this);
            this.setEntityId(result.getId());
            this.setEntityUuid(result.getUuid());
        } catch (final InvalidParameterValueException e) {
            s_logger.warn("Exception: ", e);
            throw new ServerApiException(ApiErrorCode.MALFORMED_PARAMETER_ERROR, e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LB_HEALTHCHECKPOLICY_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Create load balancer health check policy";
    }
}
