package org.apache.cloudstack.api.command.user.region.ha.gslb;

import com.cloud.region.ha.GlobalLoadBalancerRule;
import com.cloud.region.ha.GlobalLoadBalancingRulesService;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListTaggedResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.GlobalLoadBalancerResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.RegionResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listGlobalLoadBalancerRules", description = "Lists load balancer rules.", responseObject = GlobalLoadBalancerResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListGlobalLoadBalancerRuleCmd extends BaseListTaggedResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListGlobalLoadBalancerRuleCmd.class.getName());

    private static final String s_name = "listgloballoadbalancerrulesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////
    @Inject
    public GlobalLoadBalancingRulesService _gslbService;
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = GlobalLoadBalancerResponse.class, description = "the ID of the global load balancer rule")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////
    @Parameter(name = ApiConstants.REGION_ID, type = CommandType.INTEGER, entityType = RegionResponse.class, description = "region ID")
    private Integer regionId;

    public Long getId() {
        return id;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public Integer getRegionId() {
        return regionId;
    }

    @Override
    public void execute() {
        final List<GlobalLoadBalancerRule> globalLoadBalancers = _gslbService.listGlobalLoadBalancerRule(this);
        final ListResponse<GlobalLoadBalancerResponse> gslbRuleResponse = new ListResponse<>();
        final List<GlobalLoadBalancerResponse> gslbResponses = new ArrayList<>();
        if (globalLoadBalancers != null) {
            for (final GlobalLoadBalancerRule gslbRule : globalLoadBalancers) {
                final GlobalLoadBalancerResponse gslbResponse = _responseGenerator.createGlobalLoadBalancerResponse(gslbRule);
                gslbResponse.setObjectName("globalloadbalancerrule");
                gslbResponses.add(gslbResponse);
            }
        }
        gslbRuleResponse.setResponses(gslbResponses);
        gslbRuleResponse.setResponseName(getCommandName());
        this.setResponseObject(gslbRuleResponse);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
