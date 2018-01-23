package com.cloud.api.command.user.firewall;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListTaggedResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.FirewallResponse;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.network.rules.FirewallRule;
import com.cloud.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listFirewallRules", group = APICommandGroup.FirewallService, description = "Lists all firewall rules for an IP address.", responseObject = FirewallResponse.class, entityType = {FirewallRule.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListFirewallRulesCmd extends BaseListTaggedResourcesCmd implements IListFirewallRulesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListFirewallRulesCmd.class.getName());
    private static final String s_name = "listfirewallrulesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = FirewallRuleResponse.class, description = "Lists rule with the specified ID.")
    private Long id;

    @Parameter(name = ApiConstants.IP_ADDRESS_ID,
            type = CommandType.UUID,
            entityType = IPAddressResponse.class,
            description = "the ID of IP address of the firewall services")
    private Long ipAddressId;

    @Parameter(name = ApiConstants.NETWORK_ID,
            type = CommandType.UUID,
            entityType = NetworkResponse.class,
            description = "list firewall rules for certain network",
            since = "4.3")
    private Long networkId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this parameter",
            since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public Long getIpAddressId() {
        return ipAddressId;
    }

    @Override
    public FirewallRule.TrafficType getTrafficType() {
        return FirewallRule.TrafficType.Ingress;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getNetworkId() {
        return networkId;
    }

    @Override
    public Boolean getDisplay() {
        if (display != null) {
            return display;
        }
        return super.getDisplay();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends FirewallRule>, Integer> result = _firewallService.listFirewallRules(this);
        final ListResponse<FirewallResponse> response = new ListResponse<>();
        final List<FirewallResponse> fwResponses = new ArrayList<>();

        for (final FirewallRule fwRule : result.first()) {
            final FirewallResponse ruleData = _responseGenerator.createFirewallResponse(fwRule);
            ruleData.setObjectName("firewallrule");
            fwResponses.add(ruleData);
        }
        response.setResponses(fwResponses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
