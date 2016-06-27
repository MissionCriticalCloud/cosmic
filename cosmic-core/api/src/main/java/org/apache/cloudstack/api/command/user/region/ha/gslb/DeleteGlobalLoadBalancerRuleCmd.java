package org.apache.cloudstack.api.command.user.region.ha.gslb;

import com.cloud.event.EventTypes;
import com.cloud.region.ha.GlobalLoadBalancerRule;
import com.cloud.region.ha.GlobalLoadBalancingRulesService;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.GlobalLoadBalancerResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.context.CallContext;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteGlobalLoadBalancerRule", description = "Deletes a global load balancer rule.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteGlobalLoadBalancerRuleCmd extends BaseAsyncCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(DeleteGlobalLoadBalancerRuleCmd.class.getName());

    private static final String s_name = "deletegloballoadbalancerruleresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Inject
    public GlobalLoadBalancingRulesService _gslbService;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = GlobalLoadBalancerResponse.class,
            required = true,
            description = "the ID of the global load balancer rule")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_GLOBAL_LOAD_BALANCER_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "deleting global load balancer rule: " + getGlobalLoadBalancerId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.GlobalLoadBalancerRule;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.gslbSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return null;
    }

    public Long getGlobalLoadBalancerId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Deleting global Load balancer rule Id: " + getGlobalLoadBalancerId());
        final boolean result = _gslbService.deleteGlobalLoadBalancerRule(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete Global Load Balancer rule.");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final GlobalLoadBalancerRule lb = _entityMgr.findById(GlobalLoadBalancerRule.class, getGlobalLoadBalancerId());
        if (lb != null) {
            return lb.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
