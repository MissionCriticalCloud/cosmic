//

//

package com.cloud.agent.api.proxy;

/**
 * CheckConsoleProxyLoadCommand implements one-shot console proxy load-scan command
 */
public class CheckConsoleProxyLoadCommand extends ProxyCommand {

    private long proxyVmId;
    private String proxyVmName;
    private String proxyManagementIp;
    private int proxyCmdPort;

    public CheckConsoleProxyLoadCommand() {
    }

    public CheckConsoleProxyLoadCommand(final long proxyVmId, final String proxyVmName, final String proxyManagementIp, final int proxyCmdPort) {
        this.proxyVmId = proxyVmId;
        this.proxyVmName = proxyVmName;
        this.proxyManagementIp = proxyManagementIp;
        this.proxyCmdPort = proxyCmdPort;
    }

    public long getProxyVmId() {
        return proxyVmId;
    }

    public String getProxyVmName() {
        return proxyVmName;
    }

    public String getProxyManagementIp() {
        return proxyManagementIp;
    }

    public int getProxyCmdPort() {
        return proxyCmdPort;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
