//

//

package com.cloud.info;

public class ConsoleProxyInfo {

    private boolean sslEnabled;
    private String proxyAddress;
    private int proxyPort;
    private String proxyImageUrl;
    private int proxyUrlPort = 8000;

    public ConsoleProxyInfo(final int proxyUrlPort) {
        this.proxyUrlPort = proxyUrlPort;
    }

    public ConsoleProxyInfo(final boolean sslEnabled, final String proxyIpAddress, final int port, final int proxyUrlPort, final String consoleProxyUrlDomain) {
        this.sslEnabled = sslEnabled;

        if (sslEnabled) {
            final StringBuffer sb = new StringBuffer();
            if (consoleProxyUrlDomain.startsWith("*")) {
                sb.append(proxyIpAddress);
                for (int i = 0; i < proxyIpAddress.length(); i++) {
                    if (sb.charAt(i) == '.') {
                        sb.setCharAt(i, '-');
                    }
                }
                sb.append(consoleProxyUrlDomain.substring(1));//skip the *
            } else {
                //LB address
                sb.append(consoleProxyUrlDomain);
            }
            proxyAddress = sb.toString();
            proxyPort = port;
            this.proxyUrlPort = proxyUrlPort;

            proxyImageUrl = "https://" + proxyAddress;
            if (proxyUrlPort != 443) {
                proxyImageUrl += ":" + this.proxyUrlPort;
            }
        } else {
            proxyAddress = proxyIpAddress;
            proxyPort = port;
            this.proxyUrlPort = proxyUrlPort;

            proxyImageUrl = "http://" + proxyAddress;
            if (proxyUrlPort != 80) {
                proxyImageUrl += ":" + proxyUrlPort;
            }
        }
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public void setProxyAddress(final String proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyImageUrl() {
        return proxyImageUrl;
    }

    public void setProxyImageUrl(final String proxyImageUrl) {
        this.proxyImageUrl = proxyImageUrl;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(final boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }
}
