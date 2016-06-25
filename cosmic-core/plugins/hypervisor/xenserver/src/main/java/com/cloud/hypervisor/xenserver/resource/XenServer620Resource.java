package com.cloud.hypervisor.xenserver.resource;

import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.resource.ServerResource;
import org.apache.cloudstack.hypervisor.xenserver.XenserverConfigs;

import javax.ejb.Local;
import java.util.Map;
import java.util.Set;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.HostPatch;
import com.xensource.xenapi.PoolPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Local(value = ServerResource.class)
public class XenServer620Resource extends XenServer610Resource {

    private static final Logger s_logger = LoggerFactory.getLogger(XenServer620Resource.class);

    @Override
    protected void fillHostInfo(final Connection conn, final StartupRoutingCommand cmd) {
        super.fillHostInfo(conn, cmd);
        final Map<String, String> details = cmd.getHostDetails();
        final Boolean hotFix62ESP1004 = hostHasHotFix(conn, XenserverConfigs.XSHotFix62ESP1004);
        if (hotFix62ESP1004 != null && hotFix62ESP1004) {
            details.put(XenserverConfigs.XS620HotFix, XenserverConfigs.XSHotFix62ESP1004);
        } else {
            final Boolean hotFix62ESP1 = hostHasHotFix(conn, XenserverConfigs.XSHotFix62ESP1);
            if (hotFix62ESP1 != null && hotFix62ESP1) {
                details.put(XenserverConfigs.XS620HotFix, XenserverConfigs.XSHotFix62ESP1);
            }
        }
        cmd.setHostDetails(details);
    }

    protected boolean hostHasHotFix(final Connection conn, final String hotFixUuid) {
        try {
            final Host host = Host.getByUuid(conn, _host.getUuid());
            final Host.Record re = host.getRecord(conn);
            final Set<HostPatch> patches = re.patches;
            final PoolPatch poolPatch = PoolPatch.getByUuid(conn, hotFixUuid);
            for (final HostPatch patch : patches) {
                final PoolPatch pp = patch.getPoolPatch(conn);
                if (pp.equals(poolPatch) && patch.getApplied(conn)) {
                    return true;
                }
            }
        } catch (final Exception e) {
            s_logger.debug("can't get patches information for hotFix: " + hotFixUuid);
        }
        return false;
    }
}
