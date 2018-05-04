package com.cloud.offering;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.GuestType;
import com.cloud.model.enumeration.TrafficType;

/**
 * Describes network offering
 */
public interface NetworkOffering extends InternalIdentity, Identity {

    String SystemPublicNetwork = "System-Public-Network";
    String SystemControlNetwork = "System-Control-Network";
    String SystemManagementNetwork = "System-Management-Network";
    String SystemStorageNetwork = "System-Storage-Network";
    String DefaultPrivateGatewayNetworkOffering = "DefaultPrivateGatewayNetworkOffering";
    String DefaultSyncNetworkOffering = "DefaultSyncNetworkOffering";

    /**
     * @return name for the network offering.
     */
    String getName();

    /**
     * @return text to display to the end user.
     */
    String getDisplayText();

    /**
     * @return the rate in megabits per sec to which a VM's network interface is throttled to
     */
    Integer getRateMbps();

    /**
     * @return the rate megabits per sec to which a VM's multicast&broadcast traffic is throttled to
     */
    Integer getMulticastRateMbps();

    TrafficType getTrafficType();

    boolean getSpecifyVlan();

    String getTags();

    boolean isDefault();

    boolean isSystemOnly();

    Availability getAvailability();

    String getUniqueName();

    State getState();

    void setState(State state);

    GuestType getGuestType();

    Long getServiceOfferingId();

    Long getSecondaryServiceOfferingId();

    boolean getDedicatedLB();

    boolean getSharedSourceNat();

    boolean getRedundantRouter();

    boolean isConserveMode();

    boolean getElasticIp();

    boolean getAssociatePublicIP();

    boolean getElasticLb();

    boolean getSpecifyIpRanges();

    boolean isInline();

    boolean getIsPersistent();

    boolean getPublicLb();

    boolean getEgressDefaultPolicy();

    Integer getConcurrentConnections();

    boolean isKeepAliveEnabled();

    boolean getSupportsStrechedL2();

    enum Availability {
        Required, Optional
    }

    enum State {
        Disabled, Enabled
    }

    enum Detail {
        PublicLbProvider
    }
}
