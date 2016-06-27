package org.apache.cloudstack.api.command.user.nat;

import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.StaticNatRule;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.IpForwardingRuleResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.UserVmResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listIpForwardingRules", description = "List the IP forwarding rules", responseObject = FirewallRuleResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListIpForwardingRulesCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListIpForwardingRulesCmd.class.getName());

    private static final String s_name = "listipforwardingrulesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.IP_ADDRESS_ID,
            type = CommandType.UUID,
            entityType = IPAddressResponse.class,
            description = "list the rule belonging to this public IP address")
    private Long publicIpAddressId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = FirewallRuleResponse.class, description = "Lists rule with the specified ID.")
    private Long id;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            description = "Lists all rules applied to the specified VM.")
    private Long vmId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getPublicIpAddressId() {
        return publicIpAddressId;
    }

    public Long getId() {
        return id;
    }

    public Long getVmId() {
        return vmId;
    }

    @Override
    public void execute() {
        final Pair<List<? extends FirewallRule>, Integer> result =
                _rulesService.searchStaticNatRules(publicIpAddressId, id, vmId, this.getStartIndex(), this.getPageSizeVal(), this.getAccountName(), this.getDomainId(),
                        this.getProjectId(), this.isRecursive(), this.listAll());
        final ListResponse<IpForwardingRuleResponse> response = new ListResponse<>();
        final List<IpForwardingRuleResponse> ipForwardingResponses = new ArrayList<>();
        for (final FirewallRule rule : result.first()) {
            final StaticNatRule staticNatRule = _rulesService.buildStaticNatRule(rule, false);
            final IpForwardingRuleResponse resp = _responseGenerator.createIpForwardingRuleResponse(staticNatRule);
            if (resp != null) {
                ipForwardingResponses.add(resp);
            }
        }
        response.setResponses(ipForwardingResponses, result.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }
}
