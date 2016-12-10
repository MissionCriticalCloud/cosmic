package com.cloud.network.rules;

import com.cloud.api.Displayable;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

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
