package com.cloud.utils.net;

import java.net.URI;

/**
 * Download Proxy
 */
public class Proxy {
    private final String _host;
    private final int _port;
    private String _userName;
    private String _password;

    public Proxy(final String host, final int port, final String userName, final String password) {
        this._host = host;
        this._port = port;
        this._userName = userName;
        this._password = password;
    }

    public Proxy(final URI uri) {
        this._host = uri.getHost();
        this._port = uri.getPort() == -1 ? 3128 : uri.getPort();
        final String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            final String[] tokens = userInfo.split(":");
            if (tokens.length == 1) {
                this._userName = userInfo;
                this._password = "";
            } else if (tokens.length == 2) {
                this._userName = tokens[0];
                this._password = tokens[1];
            }
        }
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    public String getUserName() {
        return _userName;
    }

    public String getPassword() {
        return _password;
    }
}
