//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VM;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetVmStatsCommand.class)
public final class CitrixGetVmStatsCommandWrapper extends CommandWrapper<GetVmStatsCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixGetVmStatsCommandWrapper.class);

    @Override
    public Answer execute(final GetVmStatsCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final List<String> vmNames = command.getVmNames();
        final HashMap<String, VmStatsEntry> vmStatsNameMap = new HashMap<>();
        if (vmNames.size() == 0) {
            return new GetVmStatsAnswer(command, vmStatsNameMap);
        }
        try {

            // Determine the UUIDs of the requested VMs
            final List<String> vmUUIDs = new ArrayList<>();

            for (final String vmName : vmNames) {
                final VM vm = citrixResourceBase.getVM(conn, vmName);
                vmUUIDs.add(vm.getUuid(conn));
            }

            final HashMap<String, VmStatsEntry> vmStatsUUIDMap = citrixResourceBase.getVmStats(conn, command, vmUUIDs, command.getHostGuid());
            if (vmStatsUUIDMap == null) {
                return new GetVmStatsAnswer(command, vmStatsNameMap);
            }

            for (final Map.Entry<String, VmStatsEntry> entry : vmStatsUUIDMap.entrySet()) {
                vmStatsNameMap.put(vmNames.get(vmUUIDs.indexOf(entry.getKey())), entry.getValue());
            }

            return new GetVmStatsAnswer(command, vmStatsNameMap);
        } catch (final XenAPIException e) {
            final String msg = "Unable to get VM stats" + e.toString();
            s_logger.warn(msg, e);
            return new GetVmStatsAnswer(command, vmStatsNameMap);
        } catch (final XmlRpcException e) {
            final String msg = "Unable to get VM stats" + e.getMessage();
            s_logger.warn(msg, e);
            return new GetVmStatsAnswer(command, vmStatsNameMap);
        }
    }
}
