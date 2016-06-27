//

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
