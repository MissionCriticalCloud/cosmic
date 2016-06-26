package org.apache.cloudstack.api.command.admin.network;

import com.cloud.network.Network;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.user.network.ListNetworksCmd;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listNetworks", description = "Lists all available networks.", responseObject = NetworkResponse.class, responseView = ResponseView.Full, entityType = {Network
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
