package com.cloud.api.command.user.loadbalancer;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCustomIdCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.LoadBalancerResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.user.Account;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateLoadBalancerRule", group = APICommandGroup.LoadBalancerService, description = "Updates load balancer", responseObject = LoadBalancerResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateLoadBalancerRuleCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateLoadBalancerRuleCmd.class.getName());
    private static final String s_name = "updateloadbalancerruleresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ALGORITHM, type = CommandType.STRING, description = "load balancer algorithm (source, roundrobin, leastconn)")
    private String algorithm;

    @Parameter(name = ApiConstants.DESCRIPTION, type = CommandType.STRING, description = "the description of the load balancer rule", length = 4096)
    private String description;

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the load balancer rule to update")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the load balancer rule")
    private String loadBalancerName;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the rule to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    @Parameter(name = ApiConstants.CLIENT_TIMEOUT,
            type = CommandType.INTEGER,
            description = "the HAProxy client_timeout setting for this load balancing rule (in ms).")
    private Integer clientTimeout;

    @Parameter(name = ApiConstants.SERVER_TIMEOUT,
            type = CommandType.INTEGER,
            description = "the HAProxy server_timeout setting for this load balancing rule (in ms).")
    private Integer serverTimeout;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAlgorithm() {
        return algorithm;
    }

    public String getDescription() {
        return description;
    }

    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public Boolean getDisplay() {
        return display;
    }

    public Integer getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(final Integer clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    public Integer getServerTimeout() {
        return serverTimeout;
    }

    public void setServerTimeout(final Integer serverTimeout) {
        this.serverTimeout = serverTimeout;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LOAD_BALANCER_UPDATE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "updating load balancer rule";
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final LoadBalancer lb = _lbService.findById(getId());
        if (lb == null) {
            throw new InvalidParameterValueException("Unable to find load balancer rule by ID " + getId());
        }
        return lb.getNetworkId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Load balancer ID: " + getId());
        final LoadBalancer result = _lbService.updateLoadBalancerRule(this);
        if (result != null) {
            final LoadBalancerResponse response = _responseGenerator.createLoadBalancerResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update load balancer rule");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final LoadBalancer lb = _entityMgr.findById(LoadBalancer.class, getId());
        if (lb == null) {
            return Account.ACCOUNT_ID_SYSTEM; // bad id given, parent this command to SYSTEM so ERROR events are tracked
        }
        return lb.getAccountId();
    }

    @Override
    public void checkUuid() {
        if (this.getCustomId() != null) {
            _uuidMgr.checkUuid(this.getCustomId(), FirewallRule.class);
        }
    }
}
