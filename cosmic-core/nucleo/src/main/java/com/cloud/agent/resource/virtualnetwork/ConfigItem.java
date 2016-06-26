//

//

package com.cloud.agent.resource.virtualnetwork;

public abstract class ConfigItem {
    private String info;

    public String getInfo() {
        return info;
    }

    public void setInfo(final String info) {
        this.info = info;
    }

    public abstract String getAggregateCommand();
}
