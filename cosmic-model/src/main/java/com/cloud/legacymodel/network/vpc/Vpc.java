package com.cloud.legacymodel.network.vpc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;
import com.cloud.model.enumeration.AdvertMethod;

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
     * @return VPC source NAT list
     */
    String getSourceNatList();

    /**
     * @return VPC syslog server list
     */
    String getSyslogServerList();

    /**
     * @return VRRP advert_interval
     */
    long getAdvertInterval();

    /**
     * @return VRRP unicast subnet
     */
    String getUnicastSubnet();

    /**
     * @return VRRP advert method
     */
    AdvertMethod getAdvertMethod();

    /**
     * @return true if restart is required for the VPC; false otherwise
     */
    boolean isRestartRequired();

    boolean isDisplay();

    boolean isRedundant();

    public enum State {
        Enabled, Inactive
    }
}
