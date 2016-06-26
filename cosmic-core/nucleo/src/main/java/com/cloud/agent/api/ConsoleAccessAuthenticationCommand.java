//

//

package com.cloud.agent.api;

public class ConsoleAccessAuthenticationCommand extends AgentControlCommand {

    private String _host;
    private String _port;
    private String _vmId;
    private String _sid;
    private String _ticket;

    private boolean _isReauthenticating;

    public ConsoleAccessAuthenticationCommand() {
        _isReauthenticating = false;
    }

    public ConsoleAccessAuthenticationCommand(final String host, final String port, final String vmId, final String sid, final String ticket) {
        _host = host;
        _port = port;
        _vmId = vmId;
        _sid = sid;
        _ticket = ticket;
    }

    public String getHost() {
        return _host;
    }

    public String getPort() {
        return _port;
    }

    public String getVmId() {
        return _vmId;
    }

    public String getSid() {
        return _sid;
    }

    public String getTicket() {
        return _ticket;
    }

    public boolean isReauthenticating() {
        return _isReauthenticating;
    }

    public void setReauthenticating(final boolean value) {
        _isReauthenticating = value;
    }
}
