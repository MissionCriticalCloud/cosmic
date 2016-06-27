package com.cloud.network.rules;

import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

/**
 */
public interface HealthCheckPolicy extends InternalIdentity, Identity, Displayable {

    public long getLoadBalancerId();

    public String getpingpath();

    public String getDescription();

    public int getResponseTime();

    public int getHealthcheckInterval();

    public int getHealthcheckThresshold();

    public int getUnhealthThresshold();

    public boolean isRevoke();

    @Override
    boolean isDisplay();
}
