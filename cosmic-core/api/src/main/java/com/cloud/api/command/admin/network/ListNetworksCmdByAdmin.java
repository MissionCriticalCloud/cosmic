package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.network.ListNetworksCmd;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listNetworks", group = APICommandGroup.NetworkService, description = "Lists all available networks.", responseObject = NetworkResponse.class, responseView = ResponseView.Full,
        entityType = {Network
                .class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListNetworksCmdByAdmin extends ListNetworksCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListNetworksCmdByAdmin.class.getName());

    @Override
    public void execute() {
        final Pair<List<? extends Network>, Integer> networks = _networkService.searchForNetworks(this);
        final ListResponse<NetworkResponse> response = new ListResponse<>();
        final List<NetworkResponse> networkResponses = new ArrayList<>();
        for (final Network network : networks.first()) {
            final NetworkResponse networkResponse = _responseGenerator.createNetworkResponse(ResponseView.Full, network);
            networkResponses.add(networkResponse);
        }
        response.setResponses(networkResponses, networks.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }
}
