package com.cloud.network.rules;

import com.cloud.api.Displayable;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.utils.Pair;

import java.util.List;

/**
 */
public interface StickinessPolicy extends InternalIdentity, Identity, Displayable {

    public long getLoadBalancerId();

    public String getName();

    public String getDescription();

    public String getMethodName();

    public boolean isRevoke();

    public List<Pair<String, String>> getParams(); /* get params in Map <string,String> format */

    @Override
    boolean isDisplay();
}
