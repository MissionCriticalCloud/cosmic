package com.cloud.consoleproxy;

public interface ConsoleProxyService {

    public abstract ConsoleProxyInfo assignProxy(long dataCenterId, long userVmId);
}
