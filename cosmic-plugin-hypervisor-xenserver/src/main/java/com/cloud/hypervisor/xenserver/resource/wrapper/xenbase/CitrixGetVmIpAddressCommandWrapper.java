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

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetVmIpAddressCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.utils.net.NetUtils;

import java.util.Map;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types;
import com.xensource.xenapi.VM;
import com.xensource.xenapi.VMGuestMetrics;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetVmIpAddressCommand.class)
public final class CitrixGetVmIpAddressCommandWrapper extends CommandWrapper<GetVmIpAddressCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixGetVmIpAddressCommandWrapper.class);

    @Override
    public Answer execute(final GetVmIpAddressCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();

        final String vmName = command.getVmName();
        final String networkCidr = command.getVmNetworkCidr();
        boolean result = false;
        String errorMsg = null;
        String vmIp = null;

        try {
            final VM vm = citrixResourceBase.getVM(conn, vmName);
            final VMGuestMetrics mtr = vm.getGuestMetrics(conn);
            final VMGuestMetrics.Record rec = mtr.getRecord(conn);
            final Map<String, String> vmIpsMap = rec.networks;

            for (final String ipAddr : vmIpsMap.values()) {
                if (NetUtils.isIpWithtInCidrRange(ipAddr, networkCidr)) {
                    vmIp = ipAddr;
                    break;
                }
            }

            if (vmIp != null) {
                s_logger.debug("VM " + vmName + " ip address got retrieved " + vmIp);
                result = true;
                return new Answer(command, result, vmIp);
            }
        } catch (final Types.XenAPIException e) {
            s_logger.debug("Got exception in GetVmIpAddressCommand " + e.getMessage());
            errorMsg = "Failed to retrived vm ip addr, exception: " + e.getMessage();
        } catch (final XmlRpcException e) {
            s_logger.debug("Got exception in GetVmIpAddressCommand " + e.getMessage());
            errorMsg = "Failed to retrived vm ip addr, exception: " + e.getMessage();
        }

        return new Answer(command, result, errorMsg);
    }
}
