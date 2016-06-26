package com.cloud.network;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface VirtualRouterProvider extends InternalIdentity, Identity {
    public Type getType();

    public boolean isEnabled();

    public long getNspId();

    public enum Type {
        VirtualRouter, ElasticLoadBalancerVm, VPCVirtualRouter, InternalLbVm
    }
}
