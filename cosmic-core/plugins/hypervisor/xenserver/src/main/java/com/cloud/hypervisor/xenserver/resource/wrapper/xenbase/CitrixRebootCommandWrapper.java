//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = RebootCommand.class)
public final class CitrixRebootCommandWrapper extends CommandWrapper<RebootCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixRebootCommandWrapper.class);

    @Override
    public Answer execute(final RebootCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        s_logger.debug("7. The VM " + command.getVmName() + " is in Starting state");
        try {
            Set<VM> vms = null;
            try {
                vms = VM.getByNameLabel(conn, command.getVmName());
            } catch (final XenAPIException e0) {
                s_logger.debug("getByNameLabel failed " + e0.toString());
                return new RebootAnswer(command, "getByNameLabel failed " + e0.toString(), false);
            } catch (final Exception e0) {
                s_logger.debug("getByNameLabel failed " + e0.getMessage());
                return new RebootAnswer(command, "getByNameLabel failed", false);
            }
            for (final VM vm : vms) {
                try {
                    citrixResourceBase.rebootVM(conn, vm, vm.getNameLabel(conn));
                } catch (final Exception e) {
                    final String msg = e.toString();
                    s_logger.warn(msg, e);
                    return new RebootAnswer(command, msg, false);
                }
            }
            return new RebootAnswer(command, "reboot succeeded", true);
        } finally {
            s_logger.debug("8. The VM " + command.getVmName() + " is in Running state");
        }
    }
}
