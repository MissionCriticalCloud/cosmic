//

//

package com.cloud.agent.api;

public class PingTestCommand extends Command {

    String _computingHostIp = null;
    String _routerIp = null;
    String _privateIp = null;

    public PingTestCommand() {
    }

    public PingTestCommand(final String computingHostIp) {
        _computingHostIp = computingHostIp;
        setWait(20);
    }

    public PingTestCommand(final String routerIp, final String privateIp) {
        _routerIp = routerIp;
        _privateIp = privateIp;
        setWait(20);
    }

    public String getComputingHostIp() {
        return _computingHostIp;
    }

    public String getRouterIp() {
        return _routerIp;
    }

    public String getPrivateIp() {
        return _privateIp;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
