package com.cloud.hypervisor.xenserver.resource.wrapper.xen56;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.xenserver.resource.XenServer56Resource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.FenceAnswer;
import com.cloud.legacymodel.communication.command.FenceCommand;

import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VM;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = FenceCommand.class)
public final class XenServer56FenceCommandWrapper extends CommandWrapper<FenceCommand, Answer, XenServer56Resource> {

    private static final Logger s_logger = LoggerFactory.getLogger(XenServer56FenceCommandWrapper.class);

    @Override
    public Answer execute(final FenceCommand command, final XenServer56Resource xenServer56) {
        final Connection conn = xenServer56.getConnection();
        try {
            final Boolean alive = xenServer56.checkHeartbeat(command.getHostGuid());
            if (alive == null) {
                s_logger.debug("Failed to check heartbeat,  so unable to fence");
                return new FenceAnswer(command, false, "Failed to check heartbeat, so unable to fence");
            }
            if (alive) {
                s_logger.debug("Heart beat is still going so unable to fence");
                return new FenceAnswer(command, false, "Heartbeat is still going on unable to fence");
            }
            final Set<VM> vms = VM.getByNameLabel(conn, command.getVmName());
            for (final VM vm : vms) {
                s_logger.info("Fence command for VM " + command.getVmName());
                vm.powerStateReset(conn);
                vm.destroy(conn);
            }
            return new FenceAnswer(command);
        } catch (final XmlRpcException e) {
            s_logger.warn("Unable to fence", e);
            return new FenceAnswer(command, false, e.getMessage());
        } catch (final XenAPIException e) {
            s_logger.warn("Unable to fence", e);
            return new FenceAnswer(command, false, e.getMessage());
        } catch (final Exception e) {
            s_logger.warn("Unable to fence", e);
            return new FenceAnswer(command, false, e.getMessage());
        }
    }
}
