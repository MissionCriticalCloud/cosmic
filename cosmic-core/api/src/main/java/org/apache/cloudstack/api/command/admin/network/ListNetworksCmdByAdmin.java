// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.api.command.admin.network;

import java.util.ArrayList;
import java.util.List;

import com.cloud.network.Network;
import com.cloud.utils.Pair;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.user.network.ListNetworksCmd;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listNetworks", description = "Lists all available networks.", responseObject = NetworkResponse.class, responseView = ResponseView.Full, entityType = {Network.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListNetworksCmdByAdmin extends ListNetworksCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListNetworksCmdByAdmin.class.getName());

    @Override
    public void execute(){
        Pair<List<? extends Network>, Integer> networks = _networkService.searchForNetworks(this);
        ListResponse<NetworkResponse> response = new ListResponse<NetworkResponse>();
        List<NetworkResponse> networkResponses = new ArrayList<NetworkResponse>();
        for (Network network : networks.first()) {
            NetworkResponse networkResponse = _responseGenerator.createNetworkResponse(ResponseView.Full, network);
            networkResponses.add(networkResponse);
        }
        response.setResponses(networkResponses, networks.second());
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }
}
