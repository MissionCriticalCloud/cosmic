package org.apache.cloudstack.api.command.user.loadbalancer;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.network.rules.StickinessPolicy;
import com.cloud.user.Account;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.LBStickinessResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.context.CallContext;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listLBStickinessPolicies", description = "Lists load balancer stickiness policies.", responseObject = LBStickinessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListLBStickinessPoliciesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListLBStickinessPoliciesCmd.class.getName());

    private static final String s_name = "listlbstickinesspoliciesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////
    @Parameter(name = ApiConstants.LBID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            description = "the ID of the load balancer rule")
    private Long lbRuleId;

    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = LBStickinessResponse.class,
            description = "the ID of the load balancer stickiness policy")
    private Long id;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this parameter",
            since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    public Long getId() {
        return id;
    }

    public boolean getDisplay() {
        if (display != null) {
            return display;
        }
        return true;
    }

    @Override
    public void execute() {

        LoadBalancer lb = null;
        if (lbRuleId == null && id == null) {
            throw new InvalidParameterValueException("load balancer rule ID and stickiness policy ID can't be null");
        }

        if (id != null) {
            lb = _lbService.findLbByStickinessId(id);
            if (lb == null) {
                throw new InvalidParameterValueException("stickiness policy ID doesn't exist");
            }

            if ((lbRuleId != null) && (lbRuleId != lb.getId())) {
                throw new InvalidParameterValueException("stickiness policy ID doesn't belong to lbId" + lbRuleId);
            }
        }

        if (lbRuleId != null && lb == null) {
            lb = _lbService.findById(getLbRuleId());
        }

        final List<LBStickinessResponse> spResponses = new ArrayList<>();
        final ListResponse<LBStickinessResponse> response = new ListResponse<>();

        if (lb != null) {
            //check permissions
            final Account caller = CallContext.current().getCallingAccount();
            _accountService.checkAccess(caller, null, true, lb);
            final List<? extends StickinessPolicy> stickinessPolicies = _lbService.searchForLBStickinessPolicies(this);
            final LBStickinessResponse spResponse = _responseGenerator.createLBStickinessPolicyResponse(stickinessPolicies, lb);
            spResponses.add(spResponse);
            response.setResponses(spResponses);
        }

        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////
    public Long getLbRuleId() {
        return lbRuleId;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
