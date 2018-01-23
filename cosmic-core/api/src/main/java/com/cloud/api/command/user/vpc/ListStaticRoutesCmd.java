package com.cloud.api.command.user.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListTaggedResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PrivateGatewayResponse;
import com.cloud.api.response.StaticRouteResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.network.vpc.StaticRoute;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.InvalidParameterValueException;

import java.util.ArrayList;
import java.util.List;

@APICommand(name = "listStaticRoutes", group = APICommandGroup.VPCService, description = "Lists all static routes", responseObject = StaticRouteResponse.class, entityType = {StaticRoute.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListStaticRoutesCmd extends BaseListTaggedResourcesCmd {
    private static final String s_name = "liststaticroutesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = StaticRouteResponse.class, description = "list static route by id")
    private Long id;

    @Parameter(name = ApiConstants.VPC_ID, type = CommandType.UUID, entityType = VpcResponse.class, description = "list static routes by vpc id")
    private Long vpcId;

    @Parameter(name = ApiConstants.GATEWAY_ID, type = CommandType.UUID, entityType = PrivateGatewayResponse.class, description = "list static routes by gateway id (DEPRECATED!)")
    private Long gatewayId;

    @Parameter(name = ApiConstants.NEXT_HOP, type = CommandType.STRING, entityType = VpcResponse.class, description = "list static routes by nexthop ip address")
    private String nextHop;

    @Parameter(name = ApiConstants.CIDR, type = CommandType.STRING, entityType = VpcResponse.class, description = "list static routes by cidr")
    private String cidr;

    public Long getId() {
        return id;
    }

    public Long getVpcId() {
        return vpcId;
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    public String getNextHop() {
        return nextHop;
    }

    public String getCidr() {
        return cidr;
    }

    @Override
    public void execute() {
        checkDeprecatedParameters();

        final Pair<List<? extends StaticRoute>, Integer> result = _vpcService.listStaticRoutes(this);
        final ListResponse<StaticRouteResponse> response = new ListResponse<>();
        final List<StaticRouteResponse> routeResponses = new ArrayList<>();

        result.first().forEach(route -> {
            final StaticRouteResponse ruleData = _responseGenerator.createStaticRouteResponse(route);
            routeResponses.add(ruleData);
        });

        response.setResponses(routeResponses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    private void checkDeprecatedParameters() throws InvalidParameterValueException {
        if (gatewayId != null) {
            throw new InvalidParameterValueException("Parameter gatewayId is DEPRECATED, use vpcId and nextHop instead.");
        }
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

}
