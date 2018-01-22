package com.cloud.api.command.user.loadbalancer;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.LBStickinessResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.network.rules.StickinessPolicy;
import com.cloud.user.Account;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteLBStickinessPolicy", group = APICommandGroup.LoadBalancerService, description = "Deletes a load balancer stickiness policy.", responseObject = SuccessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteLBStickinessPolicyCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteLBStickinessPolicyCmd.class.getName());
    private static final String s_name = "deleteLBstickinessrruleresponse";
    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = LBStickinessResponse.class,
            required = true,
            description = "the ID of the LB stickiness policy")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LB_STICKINESSPOLICY_DELETE;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "deleting load balancer stickiness policy: " + getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final StickinessPolicy policy = _entityMgr.findById(StickinessPolicy.class, getId());
        if (policy == null) {
            throw new InvalidParameterValueException("Unable to find LB stickiness rule: " + id);
        }
        final LoadBalancer lb = _lbService.findById(policy.getLoadBalancerId());
        if (lb == null) {
            throw new InvalidParameterValueException("Unable to find load balancer rule for stickiness rule: " + id);
        }
        return lb.getNetworkId();
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Load balancer stickiness policy ID: " + getId());
        final boolean result = _lbService.deleteLBStickinessPolicy(getId(), true);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete load balancer stickiness policy");
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
}
