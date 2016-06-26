package com.cloud.network.vpc;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface Vpc extends ControlledEntity, Identity, InternalIdentity {

    /**
     * @return VPC name
     */
    String getName();

    /**
     * @return the id of the zone the VPC belongs to
     */
    long getZoneId();

    /**
     * @return super CIDR of the VPC. All the networks participating in VPC, should have CIDRs that are the part of the super cidr
     */
    String getCidr();

    /**
     * @return VPC state
     */
    State getState();

    /**
     * @return VPC offering id - the offering that VPC is created from
     */
    long getVpcOfferingId();

    /**
     * @return VPC display text
     */
    String getDisplayText();

    /**
     * @return VPC network domain. All networks participating in the VPC, become the part of the same network domain
     */
    String getNetworkDomain();

    /**
     * @return true if restart is required for the VPC; false otherwise
     */
    boolean isRestartRequired();

    boolean isDisplay();

    boolean isRedundant();

    /**
     * @return true if VPC is configured to use distributed router to provides one-hop forwarding and hypervisor based ACL
     */
    boolean usesDistributedRouter();

    /**
     * @return true if VPC spans multiple zones in the region
     */
    boolean isRegionLevelVpc();

    public enum State {
        Enabled, Inactive
    }
}
