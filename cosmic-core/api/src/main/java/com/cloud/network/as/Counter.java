package com.cloud.network.as;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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
