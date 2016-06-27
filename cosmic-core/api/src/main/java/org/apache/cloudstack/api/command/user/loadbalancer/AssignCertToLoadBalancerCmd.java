package org.apache.cloudstack.api.command.user.loadbalancer;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.SslCertResponse;
import org.apache.cloudstack.api.response.SuccessResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "assignCertToLoadBalancer", description = "Assigns a certificate to a load balancer rule", responseObject = SuccessResponse.class,
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
