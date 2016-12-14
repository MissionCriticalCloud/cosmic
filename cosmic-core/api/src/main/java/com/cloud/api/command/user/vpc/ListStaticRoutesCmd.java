package com.cloud.api.command.user.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListTaggedResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PrivateGatewayResponse;
import com.cloud.api.response.StaticRouteResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.network.vpc.StaticRoute;
import com.cloud.network.vpc.VpcGateway;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@APICommand(name = "listStaticRoutes", description = "Lists all static routes", responseObject = StaticRouteResponse.class, entityType = {StaticRoute.class},
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
    private String gwIpAddress;

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

    public String getGwIpAddress() {
        return gwIpAddress;
    }

    public String getCidr() {
        return cidr;
    }

    @Override
    public void execute() {
        // When we specify a gateway id, limit the search to the corresponding vpcId
        if (vpcId == null) {
            vpcId = retrieveVpcId();
        }

        final Pair<List<? extends StaticRoute>, Integer> result = _vpcService.listStaticRoutes(this);
        final ListResponse<StaticRouteResponse> response = new ListResponse<>();
        final List<StaticRouteResponse> routeResponses = new ArrayList<>();

        // Compatibility with pre 5.1
        // If gatewayId was passed, lookup its CIDR and match static routes to it
        final Optional<String> gatewayCidr = retrieveGatewayCidr();

        result.first().stream()
              .filter(route -> NetUtils.isIpWithtInCidrRange(route.getGwIpAddress(), gatewayCidr.get()))
              .forEach(route -> {
                  final StaticRouteResponse ruleData = _responseGenerator.createStaticRouteResponse(route);
                  routeResponses.add(ruleData);
              });

        response.setResponses(routeResponses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    private Optional<String> retrieveGatewayCidr() {
        final Optional<String> gatewayCidr = Optional.of("0.0.0.0/0");
        if (gatewayId != null) {
            final VpcGateway gateway = _vpcService.getVpcPrivateGateway(gatewayId);
            gatewayCidr.of(NetUtils.ipAndNetMaskToCidr(gateway.getGateway(), gateway.getNetmask()));
        }
        return gatewayCidr;
    }

    private Long retrieveVpcId() {
        if (gatewayId != null) {
            final VpcGateway gateway = _vpcService.getVpcPrivateGateway(gatewayId);
            if (gateway == null) {
                throw new InvalidParameterValueException("Private gateway with id " + gatewayId + " cannot be found");
            }
            return gateway.getVpcId();
        }
        return null;
    }
}
