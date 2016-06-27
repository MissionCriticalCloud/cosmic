package com.cloud.hypervisor.xenserver.resource;

import com.cloud.resource.ServerResource;

import javax.ejb.Local;
import java.util.Map;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Types.XenAPIException;
import org.apache.xmlrpc.XmlRpcException;

@Local(value = ServerResource.class)
public class XenServer56FP1Resource extends XenServer56Resource {

    @Override
    protected String getPatchFilePath() {
        return "scripts/vm/hypervisor/xenserver/xenserver56fp1/patch";
    }

    /**
     * When Dynamic Memory Control (DMC) is enabled -
     * xenserver allows scaling the guest memory while the guest is running
     * <p>
     * This is determined by the 'restrict_dmc' option on the host.
     * When false, scaling is allowed hence DMC is enabled
     */
    @Override
    public boolean isDmcEnabled(final Connection conn, final Host host) throws XenAPIException, XmlRpcException {
        final Map<String, String> hostParams = host.getLicenseParams(conn);
        final Boolean isDmcEnabled = hostParams.get("restrict_dmc").equalsIgnoreCase("false");
        return isDmcEnabled;
    }
}
