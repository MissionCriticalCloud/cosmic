package com.cloud.legacymodel.communication.command.startup;

import com.cloud.model.enumeration.HostType;

public class StartupProxyCommand extends StartupCommand {
    private int proxyPort;
    private long proxyVmId;

    public StartupProxyCommand() {
        super(HostType.ConsoleProxy);
        setIqn("NoIqn");
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public long getProxyVmId() {
        return proxyVmId;
    }

    public void setProxyVmId(final long proxyVmId) {
        this.proxyVmId = proxyVmId;
    }
}
