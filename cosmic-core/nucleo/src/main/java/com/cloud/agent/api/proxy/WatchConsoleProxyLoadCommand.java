//

//

package com.cloud.agent.api.proxy;

import com.cloud.agent.api.CronCommand;

public class WatchConsoleProxyLoadCommand extends ProxyCommand implements CronCommand {

    int interval;
    private long proxyVmId;
    private String proxyVmName;
    private String proxyManagementIp;
    private int proxyCmdPort;

    public WatchConsoleProxyLoadCommand(final int interval, final long proxyVmId, final String proxyVmName, final String proxyManagementIp, final int proxyCmdPort) {
        this.interval = interval;
        this.proxyVmId = proxyVmId;
        this.proxyVmName = proxyVmName;
        this.proxyManagementIp = proxyManagementIp;
        this.proxyCmdPort = proxyCmdPort;
    }

    protected WatchConsoleProxyLoadCommand() {
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
    public int getInterval() {
        return interval;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
