package com.cloud.network;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

public interface VirtualRouterProvider extends InternalIdentity, Identity {
    Type getType();

    boolean isEnabled();

    long getNspId();

    enum Type {
        VirtualRouter, VPCVirtualRouter
    }
}
