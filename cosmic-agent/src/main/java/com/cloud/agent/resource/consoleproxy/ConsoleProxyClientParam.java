package com.cloud.agent.resource.consoleproxy;

/**
 * Data object to store parameter info needed by client to connect to its host
 */
public class ConsoleProxyClientParam {

    private String clientHostAddress;
    private int clientHostPort;
    private String clientHostPassword;
    private String clientTag;
    private String ticket;

    private String clientTunnelUrl;
    private String clientTunnelSession;
    private String locale;
    private String ajaxSessionId;

    private String username;
    private String password;

    public ConsoleProxyClientParam() {
        this.clientHostPort = 0;
    }

    public String getClientHostAddress() {
        return this.clientHostAddress;
    }

    public void setClientHostAddress(final String clientHostAddress) {
        this.clientHostAddress = clientHostAddress;
    }

    public int getClientHostPort() {
        return this.clientHostPort;
    }

    public void setClientHostPort(final int clientHostPort) {
        this.clientHostPort = clientHostPort;
    }

    public String getClientHostPassword() {
        return this.clientHostPassword;
    }

    public void setClientHostPassword(final String clientHostPassword) {
        this.clientHostPassword = clientHostPassword;
    }

    public String getClientTag() {
        return this.clientTag;
    }

    public void setClientTag(final String clientTag) {
        this.clientTag = clientTag;
    }

    public String getTicket() {
        return this.ticket;
    }

    public void setTicket(final String ticket) {
        this.ticket = ticket;
    }

    public String getClientTunnelUrl() {
        return this.clientTunnelUrl;
    }

    public void setClientTunnelUrl(final String clientTunnelUrl) {
        this.clientTunnelUrl = clientTunnelUrl;
    }

    public String getClientTunnelSession() {
        return this.clientTunnelSession;
    }

    public void setClientTunnelSession(final String clientTunnelSession) {
        this.clientTunnelSession = clientTunnelSession;
    }

    public String getAjaxSessionId() {
        return this.ajaxSessionId;
    }

    public void setAjaxSessionId(final String ajaxSessionId) {
        this.ajaxSessionId = ajaxSessionId;
    }

    public String getLocale() {
        return this.locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getClientMapKey() {
        if (this.clientTag != null && !this.clientTag.isEmpty()) {
            return this.clientTag;
        }

        return this.clientHostAddress + ":" + this.clientHostPort;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
