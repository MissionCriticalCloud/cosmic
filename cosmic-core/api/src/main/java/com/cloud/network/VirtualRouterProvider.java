package com.cloud.network;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface VirtualRouterProvider extends InternalIdentity, Identity {
    Type getType();

    boolean isEnabled();

    long getNspId();

    enum Type {
        VirtualRouter, VPCVirtualRouter
    }
}
