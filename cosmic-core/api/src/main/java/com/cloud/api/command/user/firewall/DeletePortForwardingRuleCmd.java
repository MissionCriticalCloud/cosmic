package com.cloud.api.command.user.firewall;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deletePortForwardingRule", description = "Deletes a port forwarding rule", responseObject = SuccessResponse.class, entityType = {PortForwardingRule.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeletePortForwardingRuleCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeletePortForwardingRuleCmd.class.getName());
    private static final String s_name = "deleteportforwardingruleresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the port forwarding rule")
    private Long id;

    // unexposed parameter needed for events logging
    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, expose = false)
    private Long ownerId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NET_RULE_DELETE;
    }

    @Override
    public String getEventDescription() {
        return ("Deleting port forwarding rule for ID=" + id);
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
        return _entityMgr.findById(PortForwardingRule.class, id).getNetworkId();
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Rule ID: " + id);
        //revoke corresponding firewall rule first
        boolean result = _firewallService.revokeRelatedFirewallRule(id, true);
        result = result && _rulesService.revokePortForwardingRule(id, true);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete port forwarding rule");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        if (ownerId == null) {
            final PortForwardingRule rule = _entityMgr.findById(PortForwardingRule.class, id);
            if (rule == null) {
                throw new InvalidParameterValueException("Unable to find port forwarding rule by ID=" + id);
            } else {
                ownerId = _entityMgr.findById(PortForwardingRule.class, id).getAccountId();
            }
        }
        return ownerId;
    }
}
