package com.cloud.legacymodel.communication.command;

import java.util.List;

public class CheckS2SVpnConnectionsCommand extends NetworkElementCommand {
    List<String> vpnIps;

    public CheckS2SVpnConnectionsCommand(final List<String> vpnIps) {
        super();
        this.vpnIps = vpnIps;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    public List<String> getVpnIps() {
        return vpnIps;
    }
}
