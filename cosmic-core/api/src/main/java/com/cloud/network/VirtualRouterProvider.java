package com.cloud.network;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface VirtualRouterProvider extends InternalIdentity, Identity {
    public Type getType();

    public boolean isEnabled();

    public long getNspId();

    public enum Type {
        VirtualRouter, ElasticLoadBalancerVm, VPCVirtualRouter, InternalLbVm
    }
}
