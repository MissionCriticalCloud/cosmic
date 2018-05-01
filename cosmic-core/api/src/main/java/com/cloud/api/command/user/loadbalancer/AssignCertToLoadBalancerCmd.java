package com.cloud.api.command.user.loadbalancer;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.SslCertResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.rules.LoadBalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "assignCertToLoadBalancer", group = APICommandGroup.LoadBalancerService, description = "Assigns a certificate to a load balancer rule", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AssignCertToLoadBalancerCmd extends BaseAsyncCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(AssignCertToLoadBalancerCmd.class.getName());

    private static final String s_name = "assigncerttoloadbalancerresponse";

    @Parameter(name = ApiConstants.LBID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the load balancer rule")
    Long lbRuleId;

    @Parameter(name = ApiConstants.CERTIFICATE_ID,
            type = CommandType.UUID,
            entityType = SslCertResponse.class,
            required = true,
            description = "the ID of the certificate")
    Long certId;

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException, NetworkRuleConflictException {
        //To change body of implemented methods use File | Settings | File Templates.
        if (_lbService.assignCertToLoadBalancer(getLbRuleId(), getCertId())) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to assign certificate to load balancer");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final LoadBalancer lb = _entityMgr.findById(LoadBalancer.class, getLbRuleId());
        if (lb == null) {
            return Account.ACCOUNT_ID_SYSTEM; // bad id given, parent this command to SYSTEM so ERROR events are tracked
        }
        return lb.getAccountId();
    }

    public Long getLbRuleId() {
        return lbRuleId;
    }

    public Long getCertId() {
        return certId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_LB_CERT_ASSIGN;
    }

    @Override
    public String getEventDescription() {
        return "Assigning a certificate to a load balancer";
    }
}
