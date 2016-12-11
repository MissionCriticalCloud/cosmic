package com.cloud.api.command.user.firewall;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseAsyncCustomIdCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.FirewallResponse;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.TrafficType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateEgressFirewallRule", description = "Updates egress firewall rule ", responseObject = FirewallResponse.class, since = "4.4",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateEgressFirewallRuleCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateEgressFirewallRuleCmd.class.getName());

    private static final String s_name = "updateegressfirewallruleresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = FirewallRuleResponse.class, required = true, description = "the ID of the egress firewall rule")
    private Long id;

    // unexposed parameter needed for events logging
    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, expose = false)
    private Long ownerId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the rule to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        CallContext.current().setEventDetails("Rule Id: " + id);
        final FirewallRule rule = _firewallService.updateEgressFirewallRule(id, this.getCustomId(), getDisplay());

        FirewallResponse fwResponse = new FirewallResponse();
        if (rule != null) {
            fwResponse = _responseGenerator.createFirewallResponse(rule);
            setResponseObject(fwResponse);
        }
        fwResponse.setResponseName(getCommandName());
    }
    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public Boolean getDisplay() {
        return display;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        if (ownerId == null) {
            final FirewallRule rule = _entityMgr.findById(FirewallRule.class, id);
            if (rule == null || rule.getTrafficType() != TrafficType.Egress) {
                throw new InvalidParameterValueException("Unable to find egress firewall rule by ID");
            } else {
                ownerId = _entityMgr.findById(FirewallRule.class, id).getAccountId();
            }
        }
        return ownerId;
    }

    @Override
    public void checkUuid() {
        if (this.getCustomId() != null) {
            _uuidMgr.checkUuid(this.getCustomId(), FirewallRule.class);
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_FIREWALL_EGRESS_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return ("Updating egress firewall rule id=" + id);
    }
}
