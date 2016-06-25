//

//

package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.MaintainAnswer;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.hypervisor.xenserver.resource.XsHost;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.Iterator;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Types.XenAPIException;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = MaintainCommand.class)
public final class CitrixMaintainCommandWrapper extends CommandWrapper<MaintainCommand, Answer, CitrixResourceBase> {

    private static final Logger s_logger = LoggerFactory.getLogger(CitrixMaintainCommandWrapper.class);

    @Override
    public Answer execute(final MaintainCommand command, final CitrixResourceBase citrixResourceBase) {
        final Connection conn = citrixResourceBase.getConnection();
        try {

            final XsHost xsHost = citrixResourceBase.getHost();
            final String uuid = xsHost.getUuid();
            final Host host = Host.getByUuid(conn, uuid);
            // remove all tags cloud stack
            final Host.Record hr = host.getRecord(conn);

            // Adding this check because could not get the mock to work. Will push the code and fix it afterwards.
            if (hr == null) {
                s_logger.warn("Host.Record is null.");
                return new MaintainAnswer(command, false, "Host.Record is null");
            }

            final Iterator<String> it = hr.tags.iterator();
            while (it.hasNext()) {
                final String tag = it.next();
                if (tag.contains("cloud")) {
                    it.remove();
                }
            }
            host.setTags(conn, hr.tags);
            return new MaintainAnswer(command);
        } catch (final XenAPIException e) {
            s_logger.warn("Unable to put server in maintainence mode", e);
            return new MaintainAnswer(command, false, e.getMessage());
        } catch (final XmlRpcException e) {
            s_logger.warn("Unable to put server in maintainence mode", e);
            return new MaintainAnswer(command, false, e.getMessage());
        }
    }
}
