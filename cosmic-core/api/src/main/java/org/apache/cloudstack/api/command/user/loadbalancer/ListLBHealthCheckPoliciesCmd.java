package org.apache.cloudstack.api.command.user.loadbalancer;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.rules.HealthCheckPolicy;
import com.cloud.network.rules.LoadBalancer;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.LBHealthCheckResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listLBHealthCheckPolicies", description = "Lists load balancer health check policies.", responseObject = LBHealthCheckResponse.class, since = "4.2.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListLBHealthCheckPoliciesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListLBHealthCheckPoliciesCmd.class.getName());

    private static final String s_name = "listlbhealthcheckpoliciesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////
    @Parameter(name = ApiConstants.LBID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            description = "the ID of the load balancer rule")
    private Long lbRuleId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this parameter",
            since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = LBHealthCheckResponse.class, description = "the ID of the health check policy", since = "4.4")
    private Long id;

    public boolean getDisplay() {
        if (display != null) {
            return display;
        }
        return true;
    }

    @Override
    public void execute() {
        final List<LBHealthCheckResponse> hcpResponses = new ArrayList<>();
        final ListResponse<LBHealthCheckResponse> response = new ListResponse<>();
        Long lbRuleId = getLbRuleId();
        final Long hId = getId();
        if (lbRuleId == null) {
            if (hId != null) {
                lbRuleId = _lbService.findLBIdByHealtCheckPolicyId(hId);
            } else {
                throw new InvalidParameterValueException("Either load balancer rule ID or health check policy ID should be specified");
            }
        }

        final LoadBalancer lb = _lbService.findById(lbRuleId);
        if (lb != null) {
            final List<? extends HealthCheckPolicy> healthCheckPolicies = _lbService.searchForLBHealthCheckPolicies(this);
            final LBHealthCheckResponse spResponse = _responseGenerator.createLBHealthCheckPolicyResponse(healthCheckPolicies, lb);
            hcpResponses.add(spResponse);
            response.setResponses(hcpResponses);
        }

        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////
    public Long getLbRuleId() {
        return lbRuleId;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
