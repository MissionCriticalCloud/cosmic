package com.cloud.network;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

/**
 * Nic represents one nic on the VM.
 */
public interface MonitoringService extends ControlledEntity, Identity, InternalIdentity {
    @Override
    long getId();

    String getService();

    String getServiceName();

    String getServicePidFile();

    String getServicePath();

    /**
     * @return id in the CloudStack database
     */
    enum Service {
        Dhcp, LoadBalancing, Ssh, Webserver,
    }
}
