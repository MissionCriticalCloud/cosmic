package com.cloud.hypervisor.xenserver.resource;

import com.cloud.resource.ServerResource;

import javax.ejb.Local;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VM;
import org.apache.xmlrpc.XmlRpcException;

@Local(value = ServerResource.class)
public class XcpOssResource extends CitrixResourceBase {

    private static final long mem_32m = 33554432L;

    @Override
    protected String getGuestOsType(final String stdType,
                                    final String platformEmulator, final boolean bootFromCD) {
        if (stdType.equalsIgnoreCase("Debian GNU/Linux 6(64-bit)")) {
            return "Debian Squeeze 6.0 (64-bit)";
        } else if (stdType.equalsIgnoreCase("CentOS 5.6 (64-bit)")) {
            return "CentOS 5 (64-bit)";
        } else {
            return super.getGuestOsType(stdType, platformEmulator, bootFromCD);
        }
    }

    @Override
    protected String getPatchFilePath() {
        return "scripts/vm/hypervisor/xenserver/xcposs/patch";
    }

    @Override
    protected void setMemory(final Connection conn, final VM vm, final long minMemsize, final long maxMemsize) throws XmlRpcException, XenAPIException {
        vm.setMemoryLimits(conn, mem_32m, maxMemsize, minMemsize, maxMemsize);
    }
}
