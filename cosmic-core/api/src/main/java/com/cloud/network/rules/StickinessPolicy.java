package com.cloud.network.rules;

import com.cloud.utils.Pair;
import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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
