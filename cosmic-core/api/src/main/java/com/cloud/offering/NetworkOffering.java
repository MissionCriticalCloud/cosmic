package com.cloud.offering;

import com.cloud.network.Network.GuestType;
import com.cloud.network.Networks.TrafficType;
import org.apache.cloudstack.acl.InfrastructureEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

/**
 * Describes network offering
 */
public interface NetworkOffering extends InfrastructureEntity, InternalIdentity, Identity {

    public final static String SystemPublicNetwork = "System-Public-Network";
    public final static String SystemControlNetwork = "System-Control-Network";
    public final static String SystemManagementNetwork = "System-Management-Network";
    public final static String SystemStorageNetwork = "System-Storage-Network";
    public final static String SystemPrivateGatewayNetworkOffering = "System-Private-Gateway-Network-Offering";
    public final static String DefaultSharedNetworkOfferingWithSGService = "DefaultSharedNetworkOfferingWithSGService";
    public final static String DefaultIsolatedNetworkOfferingWithSourceNatService = "DefaultIsolatedNetworkOfferingWithSourceNatService";
    public final static String DefaultSharedNetworkOffering = "DefaultSharedNetworkOffering";
    public final static String DefaultIsolatedNetworkOffering = "DefaultIsolatedNetworkOffering";
    public final static String DefaultIsolatedNetworkOfferingForVpcNetworks = "DefaultIsolatedNetworkOfferingForVpcNetworks";
    public final static String DefaultIsolatedNetworkOfferingForVpcNetworksNoLB = "DefaultIsolatedNetworkOfferingForVpcNetworksNoLB";
    public final static String DefaultIsolatedNetworkOfferingForVpcNetworksWithInternalLB = "DefaultIsolatedNetworkOfferingForVpcNetworksWithInternalLB";

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

    boolean getInternalLb();

    boolean getPublicLb();

    boolean getEgressDefaultPolicy();

    Integer getConcurrentConnections();

    boolean isKeepAliveEnabled();

    boolean getSupportsStrechedL2();

    public enum Availability {
        Required, Optional
    }

    public enum State {
        Disabled, Enabled
    }

    public enum Detail {
        InternalLbProvider, PublicLbProvider
    }
}
