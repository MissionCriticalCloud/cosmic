package com.cloud.consoleproxy;

import com.cloud.utils.component.Manager;
import com.cloud.vm.ConsoleProxyVO;

public interface ConsoleProxyManager extends Manager, ConsoleProxyService {

    public static final int DEFAULT_PROXY_CAPACITY = 50;
    public static final int DEFAULT_STANDBY_CAPACITY = 10;
    public static final int DEFAULT_PROXY_VM_RAMSIZE = 1024;            // 1G
    public static final int DEFAULT_PROXY_VM_CPUMHZ = 500;                // 500 MHz

    public static final int DEFAULT_PROXY_CMD_PORT = 8001;
    public static final int DEFAULT_PROXY_VNC_PORT = 0;
    public static final int DEFAULT_PROXY_URL_PORT = 80;
    public static final int DEFAULT_PROXY_SESSION_TIMEOUT = 300000;        // 5 minutes

    public static final String ALERT_SUBJECT = "proxy-alert";
    public static final String CERTIFICATE_NAME = "CPVMCertificate";

    public ConsoleProxyManagementState getManagementState();

    public void setManagementState(ConsoleProxyManagementState state);

    public void resumeLastManagementState();

    public ConsoleProxyVO startProxy(long proxyVmId, boolean ignoreRestartSetting);

    public boolean stopProxy(long proxyVmId);

    public boolean rebootProxy(long proxyVmId);

    public boolean destroyProxy(long proxyVmId);
}
