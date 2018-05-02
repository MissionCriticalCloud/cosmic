package com.cloud.api.command.user.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListProjectAndAccountResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PrivateGatewayResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.legacymodel.network.vpc.PrivateGateway;
import com.cloud.legacymodel.network.vpc.VpcGateway;
import com.cloud.legacymodel.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listPrivateGateways", group = APICommandGroup.VPCService, description = "List private gateways", responseObject = PrivateGatewayResponse.class, entityType = {VpcGateway.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListPrivateGatewaysCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListPrivateGatewaysCmd.class.getName());

    private static final String s_name = "listprivategatewaysresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = PrivateGatewayResponse.class, description = "list private gateway by IP")
    private Long id;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, description = "list gateways by IP address")
    private String ipAddress;

    @Parameter(name = ApiConstants.NETWORK_ID, type = CommandType.STRING, description = "list gateways by network ID")
    private String networkId;

    @Parameter(name = ApiConstants.VPC_ID, type = CommandType.UUID, entityType = VpcResponse.class, description = "list gateways by VPC")
    private Long vpcId;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "list gateways by state")
    private String state;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getNetworkId() {
        return networkId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Long getVpcId() {
        return vpcId;
    }

    public Long getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    @Override
    public void execute() {
        final Pair<List<PrivateGateway>, Integer> gateways = _vpcService.listPrivateGateway(this);
        final ListResponse<PrivateGatewayResponse> response = new ListResponse<>();
        final List<PrivateGatewayResponse> projectResponses = new ArrayList<>();
        for (final PrivateGateway gateway : gateways.first()) {
            final PrivateGatewayResponse gatewayResponse = _responseGenerator.createPrivateGatewayResponse(gateway);
            projectResponses.add(gatewayResponse);
        }
        response.setResponses(projectResponses, gateways.second());
        response.setResponseName(getCommandName());

        setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }
}
