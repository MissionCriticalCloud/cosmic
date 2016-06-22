//
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
//

package com.cloud.api.commands;

import com.cloud.api.response.NiciraNvpDeviceResponse;
import com.cloud.exception.*;
import com.cloud.network.Network;
import com.cloud.network.element.NiciraNvpElementService;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.api.*;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@APICommand(name = "listNiciraNvpDeviceNetworks", responseObject = NetworkResponse.class, description = "lists network that are using a nicira nvp device",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListNiciraNvpDeviceNetworksCmd extends BaseListCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(ListNiciraNvpDeviceNetworksCmd.class.getName());
    private static final String s_name = "listniciranvpdevicenetworks";
    @Inject
    protected NiciraNvpElementService niciraNvpElementService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NICIRA_NVP_DEVICE_ID,
            type = CommandType.UUID,
            entityType = NiciraNvpDeviceResponse.class,
            required = true,
            description = "nicira nvp device ID")
    private Long niciraNvpDeviceId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getNiciraNvpDeviceId() {
        return niciraNvpDeviceId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final List<? extends Network> networks = niciraNvpElementService.listNiciraNvpDeviceNetworks(this);
            final ListResponse<NetworkResponse> response = new ListResponse<>();
            final List<NetworkResponse> networkResponses = new ArrayList<>();

            if (networks != null && !networks.isEmpty()) {
                for (final Network network : networks) {
                    final NetworkResponse networkResponse = _responseGenerator.createNetworkResponse(ResponseView.Full, network);
                    networkResponses.add(networkResponse);
                }
            }

            response.setResponses(networkResponses);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (final InvalidParameterValueException invalidParamExcp) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, invalidParamExcp.getMessage());
        } catch (final CloudRuntimeException runtimeExcp) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, runtimeExcp.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

}
