package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.PhysicalNetworkResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.PhysicalNetwork;
import com.cloud.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listPhysicalNetworks", group = APICommandGroup.NetworkService, description = "Lists physical networks", responseObject = PhysicalNetworkResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListPhysicalNetworksCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListPhysicalNetworksCmd.class.getName());

    private static final String s_name = "listphysicalnetworksresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = PhysicalNetworkResponse.class, description = "list physical network by id")
    private Long id;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the Zone ID for the physical network")
    private Long zoneId;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "search by name")
    private String networkName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {
        final Pair<List<? extends PhysicalNetwork>, Integer> result =
                _networkService.searchPhysicalNetworks(getId(), getZoneId(), this.getKeyword(), this.getStartIndex(), this.getPageSizeVal(), getNetworkName());
        if (result != null) {
            final ListResponse<PhysicalNetworkResponse> response = new ListResponse<>();
            final List<PhysicalNetworkResponse> networkResponses = new ArrayList<>();
            for (final PhysicalNetwork network : result.first()) {
                final PhysicalNetworkResponse networkResponse = _responseGenerator.createPhysicalNetworkResponse(network);
                networkResponses.add(networkResponse);
            }
            response.setResponses(networkResponses, result.second());
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to search for physical networks");
        }
    }

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getZoneId() {
        return zoneId;
    }

    public String getNetworkName() {
        return networkName;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
