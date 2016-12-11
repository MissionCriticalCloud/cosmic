package com.cloud.network.as;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface Counter extends InternalIdentity, Identity {

    String getName();

    String getValue();

    Source getSource();

    public static enum Source {
        snmp,
        cpu,
        memory
    }
}
