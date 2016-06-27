package org.apache.cloudstack.api.command.user.vpc;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.vpc.StaticRoute;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcGateway;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.PrivateGatewayResponse;
import org.apache.cloudstack.api.response.StaticRouteResponse;
import org.apache.cloudstack.api.response.VpcResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createStaticRoute", description = "Creates a static route", responseObject = StaticRouteResponse.class, entityType = {StaticRoute.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateStaticRouteCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateStaticRouteCmd.class.getName());
    private static final String s_name = "createstaticrouteresponse";
    @Parameter(name = ApiConstants.VPC_ID,
            type = CommandType.UUID,
            entityType = VpcResponse.class,
            description = "The VPC id we are creating static route for.")
    private Long vpcId;

    @Parameter(name = ApiConstants.CIDR, required = true, type = CommandType.STRING, description = "The CIDR to create the static route for")
    private String cidr;

    @Parameter(name = ApiConstants.NEXT_HOP, type = CommandType.STRING, description = "Ip address of the nexthop to route the CIDR to")
    private String gwIpAddress;

    @Parameter(name = ApiConstants.GATEWAY_ID,
            type = CommandType.UUID,
            entityType = PrivateGatewayResponse.class,
            description = "The private gateway id to get the ipaddress from (DEPRECATED!).")
    private Long gatewayId;

    public String getCidr() {
        return cidr;
    }

    public String getGwIpAddress() {
        Compatibility();
        return gwIpAddress;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public void create() throws ResourceAllocationException {
        try {
            final StaticRoute result = _vpcService.createStaticRoute(getVpcId(), getCidr(), getGwIpAddress());
            setEntityId(result.getId());
            setEntityUuid(result.getUuid());
        } catch (final NetworkRuleConflictException ex) {
            s_logger.info("Network rule conflict: " + ex.getMessage());
            s_logger.trace("Network rule conflict: ", ex);
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_STATIC_ROUTE_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "Applying static route. Static route Id: " + getEntityId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.StaticRoute;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.vpcSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getVpcId();
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    public Long getVpcId() {
        Compatibility();
        return vpcId;
    }

    // Compatibility with < 5.2
    private void Compatibility() {
        if (getGatewayId() != null) {
            final VpcGateway gateway = _vpcService.getVpcPrivateGateway(getGatewayId());
            gwIpAddress = gateway.getGateway();
            vpcId = gateway.getVpcId();
        }
        CheckParameters();
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    private void CheckParameters() throws InvalidParameterValueException {
        if (vpcId == null) {
            throw new InvalidParameterValueException(
                    "VpcId should not be empty. Either specify VpcId (recommended) or specify gatewayId (deprecated).");
        }
        if (gwIpAddress == null) {
            throw new InvalidParameterValueException(
                    "Parameter nexthop should not be empty. Either specify nexthop ip address (recommended) or specify gatewayId (deprecated).");
        }
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        boolean success = false;
        StaticRoute route = null;
        try {
            CallContext.current().setEventDetails("Static route Id: " + getEntityId());
            success = _vpcService.applyStaticRoute(getEntityId());
            // State is different after the route is applied, so retrieve the object only here
            route = _entityMgr.findById(StaticRoute.class, getEntityId());
            StaticRouteResponse routeResponse = new StaticRouteResponse();
            if (route != null) {
                routeResponse = _responseGenerator.createStaticRouteResponse(route);
                setResponseObject(routeResponse);
            }
            routeResponse.setResponseName(getCommandName());
        } finally {
            if (!success || route == null) {
                _vpcService.revokeStaticRoute(getEntityId());
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create static route");
            }
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return _entityMgr.findById(Vpc.class, getVpcId()).getAccountId();
    }
}
