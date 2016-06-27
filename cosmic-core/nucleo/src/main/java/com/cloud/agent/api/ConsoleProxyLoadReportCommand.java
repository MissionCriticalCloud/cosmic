//

//

package com.cloud.agent.api;

public class ConsoleProxyLoadReportCommand extends AgentControlCommand {

    private long _proxyVmId;
    private String _loadInfo;

    public ConsoleProxyLoadReportCommand() {
    }

    public ConsoleProxyLoadReportCommand(final long proxyVmId, final String loadInfo) {
        _proxyVmId = proxyVmId;
        _loadInfo = loadInfo;
    }

    public long getProxyVmId() {
        return _proxyVmId;
    }

    public String getLoadInfo() {
        return _loadInfo;
    }
}
