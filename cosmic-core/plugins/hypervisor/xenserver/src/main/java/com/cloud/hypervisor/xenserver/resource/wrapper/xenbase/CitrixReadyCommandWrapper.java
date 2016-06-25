//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.ReadyAnswer;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VM;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = ReadyCommand.class)
public final class CitrixReadyCommandWrapper extends CommandWrapper<ReadyCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixReadyCommandWrapper.class);

    @Override
    public Answer execute(final ReadyCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        final Long dcId = command.getDataCenterId();
        // Ignore the result of the callHostPlugin. Even if unmounting the
        // snapshots dir fails, let Ready command
        // succeed.
        citrixResourceBase.umountSnapshotDir(conn, dcId);

        citrixResourceBase.setupLinkLocalNetwork(conn);
        // try to destroy CD-ROM device for all system VMs on this host
        try {
            final Host host = Host.getByUuid(conn, citrixResourceBase.getHost().getUuid());
            final Set<VM> vms = host.getResidentVMs(conn);
            for (final VM vm : vms) {
                citrixResourceBase.destroyPatchVbd(conn, vm.getNameLabel(conn));
            }
        } catch (final Exception e) {
        }
        try {
            final boolean result = citrixResourceBase.cleanupHaltedVms(conn);
            if (!result) {
                return new ReadyAnswer(command, "Unable to cleanup halted vms");
            }
        } catch (final XenAPIException e) {
            s_logger.warn("Unable to cleanup halted vms", e);
            return new ReadyAnswer(command, "Unable to cleanup halted vms");
        } catch (final XmlRpcException e) {
            s_logger.warn("Unable to cleanup halted vms", e);
            return new ReadyAnswer(command, "Unable to cleanup halted vms");
        }

        return new ReadyAnswer(command);
    }
}
