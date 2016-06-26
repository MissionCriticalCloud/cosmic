// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.servlet;

// To maintain independency of console proxy project, we duplicate this class from console proxy project
public class ConsoleProxyClientParam {
    private String clientHostAddress;
    private int clientHostPort;
    private String clientHostPassword;
    private String clientTag;
    private String ticket;
    private String locale;
    private String clientTunnelUrl;
    private String clientTunnelSession;

    private String ajaxSessionId;
    private String username;
    private String password;

    public ConsoleProxyClientParam() {
        clientHostPort = 0;
    }

    public String getClientHostAddress() {
        return clientHostAddress;
    }

    public void setClientHostAddress(final String clientHostAddress) {
        this.clientHostAddress = clientHostAddress;
    }

    public int getClientHostPort() {
        return clientHostPort;
    }

    public void setClientHostPort(final int clientHostPort) {
        this.clientHostPort = clientHostPort;
    }

    public String getClientHostPassword() {
        return clientHostPassword;
    }

    public void setClientHostPassword(final String clientHostPassword) {
        this.clientHostPassword = clientHostPassword;
    }

    public String getClientTag() {
        return clientTag;
    }

    public void setClientTag(final String clientTag) {
        this.clientTag = clientTag;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(final String ticket) {
        this.ticket = ticket;
    }

    public String getClientTunnelUrl() {
        return clientTunnelUrl;
    }

    public void setClientTunnelUrl(final String clientTunnelUrl) {
        this.clientTunnelUrl = clientTunnelUrl;
    }

    public String getClientTunnelSession() {
        return clientTunnelSession;
    }

    public void setClientTunnelSession(final String clientTunnelSession) {
        this.clientTunnelSession = clientTunnelSession;
    }

    public String getAjaxSessionId() {
        return ajaxSessionId;
    }

    public void setAjaxSessionId(final String ajaxSessionId) {
        this.ajaxSessionId = ajaxSessionId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getClientMapKey() {
        if (clientTag != null && !clientTag.isEmpty()) {
            return clientTag;
        }

        return clientHostAddress + ":" + clientHostPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
