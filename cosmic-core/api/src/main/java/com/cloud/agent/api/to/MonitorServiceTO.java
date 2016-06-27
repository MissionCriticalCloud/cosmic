package com.cloud.agent.api.to;

import org.apache.cloudstack.api.InternalIdentity;

public class MonitorServiceTO implements InternalIdentity {
    long id;
    String service;
    String processname;
    String serviceName;
    String servicePath;
    String pidFile;
    boolean isDefault;

    protected MonitorServiceTO() {
    }

    public MonitorServiceTO(final String service, final String processname, final String serviceName, final String servicepath, final String pidFile, final boolean isDefault) {
        this.service = service;
        this.processname = processname;
        this.serviceName = serviceName;
        this.servicePath = servicepath;
        this.pidFile = pidFile;
        this.isDefault = isDefault;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getPidFile() {
        return pidFile;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServicePath() {
        return servicePath;
    }

    public String getProcessname() {
        return processname;
    }
}
