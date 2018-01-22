package com.cloud.api.command.user.vpn;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListProjectAndAccountResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.VpnUsersResponse;
import com.cloud.network.VpnUser;
import com.cloud.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listVpnUsers", group = APICommandGroup.VPNService, description = "Lists vpn users", responseObject = VpnUsersResponse.class, entityType = {VpnUser.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListVpnUsersCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListVpnUsersCmd.class.getName());

    private static final String s_name = "listvpnusersresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VpnUsersResponse.class, description = "The uuid of the Vpn user")
    private Long id;

    @Parameter(name = ApiConstants.USERNAME, type = CommandType.STRING, description = "the username of the vpn user.")
    private String userName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return userName;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends VpnUser>, Integer> vpnUsers = _ravService.searchForVpnUsers(this);

        final ListResponse<VpnUsersResponse> response = new ListResponse<>();
        final List<VpnUsersResponse> vpnResponses = new ArrayList<>();
        for (final VpnUser vpnUser : vpnUsers.first()) {
            vpnResponses.add(_responseGenerator.createVpnUserResponse(vpnUser));
        }

        response.setResponses(vpnResponses, vpnUsers.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
