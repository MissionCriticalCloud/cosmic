//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class MonitorService extends ConfigBase {
    public String config, disableMonitoring;

    public MonitorService() {
        super(ConfigBase.MONITORSERVICE);
    }

    public MonitorService(final String config, final String disableMonitoring) {
        super(ConfigBase.MONITORSERVICE);
        this.config = config;
        this.disableMonitoring = disableMonitoring;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(final String config) {
        this.config = config;
    }

    public String getDisableMonitoring() {
        return disableMonitoring;
    }

    public void setDisableMonitoring(final String disableMonitoring) {
        this.disableMonitoring = disableMonitoring;
    }
}
