//

//

package com.cloud.network.nicira;

import java.util.List;

public abstract class AccessConfiguration<T extends AccessRule> extends BaseNiciraNamedEntity {

    protected List<T> logicalPortEgressRules;
    protected List<T> logicalPortIngressRules;

    public List<T> getLogicalPortEgressRules() {
        return logicalPortEgressRules;
    }

    public void setLogicalPortEgressRules(final List<T> logicalPortEgressRules) {
        this.logicalPortEgressRules = logicalPortEgressRules;
    }

    public List<T> getLogicalPortIngressRules() {
        return logicalPortIngressRules;
    }

    public void setLogicalPortIngressRules(final List<T> logicalPortIngressRules) {
        this.logicalPortIngressRules = logicalPortIngressRules;
    }
}
