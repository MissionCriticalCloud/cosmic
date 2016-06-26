//

//

package com.cloud.agent.api;

import com.cloud.host.Host;

import java.util.Map;

public class PingRoutingCommand extends PingCommand {

    Map<String, HostVmStateReportEntry> _hostVmStateReport;

    boolean _gatewayAccessible = true;
    boolean _vnetAccessible = true;

    protected PingRoutingCommand() {
    }

    public PingRoutingCommand(final Host.Type type, final long id, final Map<String, HostVmStateReportEntry> hostVmStateReport) {
        super(type, id);
        this._hostVmStateReport = hostVmStateReport;
    }

    public Map<String, HostVmStateReportEntry> getHostVmStateReport() {
        return this._hostVmStateReport;
    }

    public boolean isGatewayAccessible() {
        return _gatewayAccessible;
    }

    public void setGatewayAccessible(final boolean gatewayAccessible) {
        _gatewayAccessible = gatewayAccessible;
    }

    public boolean isVnetAccessible() {
        return _vnetAccessible;
    }

    public void setVnetAccessible(final boolean vnetAccessible) {
        _vnetAccessible = vnetAccessible;
    }
}
