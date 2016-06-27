//

//

package com.cloud.agent.api;

public class ConsoleAccessAuthenticationAnswer extends AgentControlAnswer {

    private boolean _success;

    private boolean _isReauthenticating;
    private String _host;
    private int _port;

    private String _tunnelUrl;
    private String _tunnelSession;

    public ConsoleAccessAuthenticationAnswer() {
        _success = false;
        _isReauthenticating = false;
        _port = 0;
    }

    public ConsoleAccessAuthenticationAnswer(final Command cmd, final boolean success) {
        super(cmd);
        _success = success;
    }

    public boolean succeeded() {
        return _success;
    }

    public void setSuccess(final boolean value) {
        _success = value;
    }

    public boolean isReauthenticating() {
        return _isReauthenticating;
    }

    public void setReauthenticating(final boolean value) {
        _isReauthenticating = value;
    }

    public String getHost() {
        return _host;
    }

    public void setHost(final String host) {
        _host = host;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(final int port) {
        _port = port;
    }

    public String getTunnelUrl() {
        return _tunnelUrl;
    }

    public void setTunnelUrl(final String tunnelUrl) {
        _tunnelUrl = tunnelUrl;
    }

    public String getTunnelSession() {
        return _tunnelSession;
    }

    public void setTunnelSession(final String tunnelSession) {
        _tunnelSession = tunnelSession;
    }
}
