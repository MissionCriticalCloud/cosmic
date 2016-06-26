//

//

package com.cloud.agent.api;

import java.util.HashMap;
import java.util.Map;

public class PerformanceMonitorCommand extends Command {

    Map<String, String> params = new HashMap<>();

    public PerformanceMonitorCommand() {
    }

    public PerformanceMonitorCommand(final Map<String, String> params, final int wait) {
        setWait(wait);
        this.params = params;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(final Map<String, String> params) {
        this.params = params;
    }
}
