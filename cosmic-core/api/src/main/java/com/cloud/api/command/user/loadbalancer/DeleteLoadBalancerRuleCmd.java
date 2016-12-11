package com.cloud.api.command.user.loadbalancer;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteLoadBalancerRule", description = "Deletes a load balancer rule.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteLoadBalancerRuleCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteLoadBalancerRuleCmd.class.getName());
    private static final String s_name = "deleteloadbalancerruleresponse";
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the load balancer rule")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LOAD_BALANCER_DELETE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "deleting load balancer: " + getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.FirewallRule;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final LoadBalancer lb = _lbService.findById(id);
        if (lb == null) {
            throw new InvalidParameterValueException("Unable to find load balancer rule: " + id);
        }
        return lb.getNetworkId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Load balancer ID: " + getId());
        boolean result = _firewallService.revokeRelatedFirewallRule(id, true);
        result = result && _lbService.deleteLoadBalancerRule(id, true);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete load balancer");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final LoadBalancer lb = _entityMgr.findById(LoadBalancer.class, getId());
        if (lb != null) {
            return lb.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
