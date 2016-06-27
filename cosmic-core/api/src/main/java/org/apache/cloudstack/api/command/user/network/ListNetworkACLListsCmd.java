package org.apache.cloudstack.api.command.user.network;

import com.cloud.network.vpc.NetworkACL;
import com.cloud.utils.Pair;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkACLResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.VpcResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listNetworkACLLists", description = "Lists all network ACLs", responseObject = NetworkACLResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListNetworkACLListsCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListNetworkACLListsCmd.class.getName());

    private static final String s_name = "listnetworkacllistsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = NetworkACLResponse.class, description = "Lists network ACL with the specified ID.")
    private Long id;

    @Parameter(name = ApiConstants.NETWORK_ID, type = CommandType.UUID, entityType = NetworkResponse.class, description = "list network ACLs by network ID")
    private Long networkId;

    @Parameter(name = ApiConstants.VPC_ID, type = CommandType.UUID, entityType = VpcResponse.class, description = "list network ACLs by VPC ID")
    private Long vpcId;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "list network ACLs by specified name")
    private String name;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "list resources by display flag; only ROOT admin is eligible to pass this parameter",
            since = "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getNetworkId() {
        return networkId;
    }

    public Long getId() {
        return id;
    }

    public Long getVpcId() {
        return vpcId;
    }

    public String getName() {
        return name;
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
        final Pair<List<? extends NetworkACL>, Integer> result = _networkACLService.listNetworkACLs(this);
        final ListResponse<NetworkACLResponse> response = new ListResponse<>();
        final List<NetworkACLResponse> aclResponses = new ArrayList<>();

        for (final NetworkACL acl : result.first()) {
            final NetworkACLResponse aclResponse = _responseGenerator.createNetworkACLResponse(acl);
            aclResponses.add(aclResponse);
        }
        response.setResponses(aclResponses, result.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
