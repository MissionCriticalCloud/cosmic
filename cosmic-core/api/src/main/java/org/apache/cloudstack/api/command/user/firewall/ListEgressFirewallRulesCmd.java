package org.apache.cloudstack.api.command.user.firewall;

import com.cloud.network.rules.FirewallRule;
import com.cloud.utils.Pair;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListTaggedResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.FirewallResponse;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listEgressFirewallRules", description = "Lists all egress firewall rules for network ID.", responseObject = FirewallResponse.class, entityType =
        {FirewallRule.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListEgressFirewallRulesCmd extends BaseListTaggedResourcesCmd implements IListFirewallRulesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListEgressFirewallRulesCmd.class.getName());
    private static final String s_name = "listegressfirewallrulesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = FirewallRuleResponse.class, description = "Lists rule with the specified ID.")
    private Long id;

    @Parameter(name = ApiConstants.NETWORK_ID,
            type = CommandType.UUID,
            entityType = NetworkResponse.class,
            description = "the network ID for the egress firewall services")
    private Long networkId;

    @Parameter(name = ApiConstants.IP_ADDRESS_ID,
            type = CommandType.UUID,
            entityType = IPAddressResponse.class,
            description = "the ID of IP address of the firewall services")
    private Long ipAddressId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this parameter",
            since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getIpAddressId() {
        return ipAddressId;
    }

    public FirewallRule.TrafficType getTrafficType() {
        return FirewallRule.TrafficType.Egress;
    }

    public Long getId() {
        return id;
    }

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

        if (result != null) {
            for (final FirewallRule fwRule : result.first()) {
                final FirewallResponse ruleData = _responseGenerator.createFirewallResponse(fwRule);
                ruleData.setObjectName("firewallrule");
                fwResponses.add(ruleData);
            }
            response.setResponses(fwResponses, result.second());
        }
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
