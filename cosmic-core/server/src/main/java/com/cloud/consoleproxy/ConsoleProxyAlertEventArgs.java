package com.cloud.consoleproxy;

import com.cloud.utils.events.EventArgs;
import com.cloud.vm.ConsoleProxyVO;

public class ConsoleProxyAlertEventArgs extends EventArgs {

    public static final int PROXY_CREATED = 1;
    public static final int PROXY_UP = 2;
    public static final int PROXY_DOWN = 3;
    public static final int PROXY_CREATE_FAILURE = 4;
    public static final int PROXY_START_FAILURE = 5;
    public static final int PROXY_FIREWALL_ALERT = 6;
    public static final int PROXY_STORAGE_ALERT = 7;
    public static final int PROXY_REBOOTED = 8;
    private static final long serialVersionUID = 23773987551479885L;
    private final int type;
    private final long zoneId;
    private final long proxyId;
    private final ConsoleProxyVO proxy;
    private final String message;

    public ConsoleProxyAlertEventArgs(final int type, final long zoneId, final long proxyId, final ConsoleProxyVO proxy, final String message) {

        super(ConsoleProxyManager.ALERT_SUBJECT);
        this.type = type;
        this.zoneId = zoneId;
        this.proxyId = proxyId;
        this.proxy = proxy;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public long getZoneId() {
        return zoneId;
    }

    public long getProxyId() {
        return proxyId;
    }

    public ConsoleProxyVO getProxy() {
        return proxy;
    }

    public String getMessage() {
        return message;
    }
}
