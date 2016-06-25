//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.UnPlugNicAnswer;
import com.cloud.agent.api.UnPlugNicCommand;
import com.cloud.agent.api.to.NicTO;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Network;
import com.xensource.xenapi.VIF;
import com.xensource.xenapi.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = UnPlugNicCommand.class)
public final class CitrixUnPlugNicCommandWrapper extends CommandWrapper<UnPlugNicCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixUnPlugNicCommandWrapper.class);

    @Override
    public Answer execute(final UnPlugNicCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final String vmName = command.getVmName();
        try {
            final Set<VM> vms = VM.getByNameLabel(conn, vmName);
            if (vms == null || vms.isEmpty()) {
                return new UnPlugNicAnswer(command, false, "Can not find VM " + vmName);
            }
            final VM vm = vms.iterator().next();
            final NicTO nic = command.getNic();
            final String mac = nic.getMac();
            final VIF vif = citrixResourceBase.getVifByMac(conn, vm, mac);
            if (vif != null) {
                vif.unplug(conn);
                final Network network = vif.getNetwork(conn);
                vif.destroy(conn);
                try {
                    if (network.getNameLabel(conn).startsWith("VLAN")) {
                        citrixResourceBase.disableVlanNetwork(conn, network);
                    }
                } catch (final Exception e) {
                }
            }
            return new UnPlugNicAnswer(command, true, "success");
        } catch (final Exception e) {
            final String msg = " UnPlug Nic failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new UnPlugNicAnswer(command, false, msg);
        }
    }
}
