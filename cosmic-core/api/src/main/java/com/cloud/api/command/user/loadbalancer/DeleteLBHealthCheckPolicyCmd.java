package com.cloud.api.command.user.loadbalancer;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.LBHealthCheckResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.network.LoadBalancer;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.rules.HealthCheckPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteLBHealthCheckPolicy", group = APICommandGroup.LoadBalancerService, description = "Deletes a load balancer health check policy.", responseObject = SuccessResponse.class,
        since = "4.2.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteLBHealthCheckPolicyCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteLBHealthCheckPolicyCmd.class.getName());
    private static final String s_name = "deletelbhealthcheckpolicyresponse";
    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = LBHealthCheckResponse.class,
            required = true,
            description = "the ID of the load balancer health check policy")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LB_HEALTHCHECKPOLICY_DELETE;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "deleting load balancer health check policy: " + getId();
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
        final HealthCheckPolicy policy = _entityMgr.findById(HealthCheckPolicy.class, getId());
        if (policy == null) {
            throw new InvalidParameterValueException("Unable to find load balancer health check rule: " + id);
        }
        final LoadBalancer lb = _lbService.findById(policy.getLoadBalancerId());
        if (lb == null) {
            throw new InvalidParameterValueException("Unable to find load balancer rule for health check rule: " + id);
        }
        return lb.getNetworkId();
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Load balancer health check policy Id: " + getId());
        final boolean result = _lbService.deleteLBHealthCheckPolicy(getId(), true);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete load balancer health check policy");
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
