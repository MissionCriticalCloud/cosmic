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

import com.cloud.exception.*;
import com.cloud.host.Host;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.api.*;
import org.apache.cloudstack.api.response.NetworkDeviceResponse;
import org.apache.cloudstack.network.ExternalNetworkDeviceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;


@APICommand(name = "addNetworkDevice",
        description = "Adds a network device of one of the following types: ExternalDhcp, ExternalFirewall, ExternalLoadBalancer",
        responseObject = NetworkDeviceResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddNetworkDeviceCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddNetworkDeviceCmd.class);
    private static final String s_name = "addnetworkdeviceresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Inject
    ExternalNetworkDeviceManager nwDeviceMgr;
    @Parameter(name = ApiConstants.NETWORK_DEVICE_TYPE,
            type = CommandType.STRING,
            description = "Network device type, now supports ExternalDhcp, JuniperSRXFirewall, PaloAltoFirewall")
    private String type;

    @Parameter(name = ApiConstants.NETWORK_DEVICE_PARAMETER_LIST, type = CommandType.MAP, description = "parameters for network device")
    private Map paramList;

    public String getDeviceType() {
        return type;
    }

    public Map getParamList() {
        return paramList;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final Host device = nwDeviceMgr.addNetworkDevice(this);
            final NetworkDeviceResponse response = nwDeviceMgr.getApiResponse(device);
            response.setObjectName("networkdevice");
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } catch (final InvalidParameterValueException ipve) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, ipve.getMessage());
        } catch (final CloudRuntimeException cre) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, cre.getMessage());
        }

    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        // TODO Auto-generated method stub
        return 0;
    }

}
