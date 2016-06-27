package com.cloud.network;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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
