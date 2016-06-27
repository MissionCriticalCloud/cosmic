package com.cloud.network.dao;

import com.cloud.network.Network;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.Mode;
import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.acl.ControlledEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

/**
 * NetworkConfigurationVO contains information about a specific network.
 */
@Entity
@Table(name = "networks")
public class NetworkVO implements Network {
    @Column(name = "display_network", updatable = true, nullable = false)
    protected boolean displayNetwork = true;
    @Id
    @TableGenerator(name = "networks_sq", table = "sequence", pkColumnName = "name", valueColumnName = "value", pkColumnValue = "networks_seq", allocationSize = 1)
    @Column(name = "id")
    long id;
    @Column(name = "mode")
    @Enumerated(value = EnumType.STRING)
    Mode mode;
    @Column(name = "broadcast_domain_type")
    @Enumerated(value = EnumType.STRING)
    BroadcastDomainType broadcastDomainType;
    @Column(name = "traffic_type")
    @Enumerated(value = EnumType.STRING)
    TrafficType trafficType;
    @Column(name = "name")
    String name;
    @Column(name = "display_text")
    String displayText;
    @Column(name = "broadcast_uri")
    URI broadcastUri;
    @Column(name = "gateway")
    String gateway;
    @Column(name = "cidr")
    String cidr;
    @Column(name = "network_cidr")
    String networkCidr;
    @Column(name = "network_offering_id")
    long networkOfferingId;
    @Column(name = "vpc_id")
    Long vpcId;
    @Column(name = "physical_network_id")
    Long physicalNetworkId;
    @Column(name = "data_center_id")
    long dataCenterId;
    @Column(name = "related")
    long related;
    @Column(name = "guru_name")
    String guruName;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    State state;
    @Column(name = "redundant")
    boolean isRedundant;
    @Column(name = "dns1")
    String dns1;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "account_id")
    long accountId;
    @Column(name = "set_fields")
    long setFields;
    @TableGenerator(name = "mac_address_seq", table = "op_networks", pkColumnName = "id", valueColumnName = "mac_address_seq", allocationSize = 1)
    @Transient
    long macAddress = 1;
    @Column(name = "guru_data", length = 1024)
    String guruData;
    @Column(name = "dns2")
    String dns2;
    @Column(name = "network_domain")
    String networkDomain;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "reservation_id")
    String reservationId;
    @Column(name = "uuid")
    String uuid;
    @Column(name = "guest_type")
    @Enumerated(value = EnumType.STRING)
    Network.GuestType guestType;
    @Column(name = "acl_type")
    @Enumerated(value = EnumType.STRING)
    ControlledEntity.ACLType aclType;
    @Column(name = "restart_required")
    boolean restartRequired = false;
    @Column(name = "specify_ip_ranges")
    boolean specifyIpRanges = false;
    @Column(name = "ip6_gateway")
    String ip6Gateway;
    @Column(name = "ip6_cidr")
    String ip6Cidr;
    @Column(name = "network_acl_id")
    Long networkACLId;

    @Column(name = "streched_l2")
    boolean strechedL2Network = false;

    public NetworkVO() {
        uuid = UUID.randomUUID().toString();
    }

    public NetworkVO(final long id, final Network that, final long offeringId, final String guruName, final long domainId, final long accountId, final long related, final String
            name, final String displayText,
                     final String networkDomain, final GuestType guestType, final long dcId, final Long physicalNetworkId, final ACLType aclType, final boolean specifyIpRanges,
                     final Long vpcId, final boolean
                             isRedundant) {
        this(id,
                that.getTrafficType(),
                that.getMode(),
                that.getBroadcastDomainType(),
                offeringId,
                domainId,
                accountId,
                related,
                name,
                displayText,
                networkDomain,
                guestType,
                dcId,
                physicalNetworkId,
                aclType,
                specifyIpRanges,
                vpcId,
                isRedundant);
        gateway = that.getGateway();
        cidr = that.getCidr();
        networkCidr = that.getNetworkCidr();
        broadcastUri = that.getBroadcastUri();
        broadcastDomainType = that.getBroadcastDomainType();
        this.guruName = guruName;
        state = that.getState();
        if (state == null) {
            state = State.Allocated;
        }
        uuid = UUID.randomUUID().toString();
        ip6Gateway = that.getIp6Gateway();
        ip6Cidr = that.getIp6Cidr();
    }

    /**
     * Constructor for the actual DAO object.
     *
     * @param trafficType
     * @param mode
     * @param broadcastDomainType
     * @param networkOfferingId
     * @param domainId
     * @param accountId
     * @param name
     * @param displayText
     * @param networkDomain
     * @param guestType           TODO
     * @param aclType             TODO
     * @param specifyIpRanges     TODO
     * @param vpcId               TODO
     * @param dataCenterId
     */
    public NetworkVO(final long id, final TrafficType trafficType, final Mode mode, final BroadcastDomainType broadcastDomainType, final long networkOfferingId, final long
            domainId, final long accountId,
                     final long related, final String name, final String displayText, final String networkDomain, final GuestType guestType, final long dcId, final Long
                             physicalNetworkId, final ACLType aclType,
                     final boolean specifyIpRanges, final Long vpcId, final boolean isRedundant) {
        this(trafficType, mode, broadcastDomainType, networkOfferingId, State.Allocated, dcId, physicalNetworkId, isRedundant);
        this.domainId = domainId;
        this.accountId = accountId;
        this.related = related;
        this.id = id;
        this.name = name;
        this.displayText = displayText;
        this.aclType = aclType;
        this.networkDomain = networkDomain;
        uuid = UUID.randomUUID().toString();
        this.guestType = guestType;
        this.specifyIpRanges = specifyIpRanges;
        this.vpcId = vpcId;
    }

    /**
     * Constructor to be used for the adapters because it only initializes what's needed.
     *
     * @param trafficType
     * @param mode
     * @param broadcastDomainType
     * @param networkOfferingId
     * @param state               TODO
     * @param dataCenterId
     * @param physicalNetworkId   TODO
     */
    public NetworkVO(final TrafficType trafficType, final Mode mode, final BroadcastDomainType broadcastDomainType, final long networkOfferingId, final State state, final long
            dataCenterId,
                     final Long physicalNetworkId, final boolean isRedundant) {
        this.trafficType = trafficType;
        this.mode = mode;
        this.broadcastDomainType = broadcastDomainType;
        this.networkOfferingId = networkOfferingId;
        this.dataCenterId = dataCenterId;
        this.physicalNetworkId = physicalNetworkId;
        this.isRedundant = isRedundant;
        if (state == null) {
            this.state = State.Allocated;
        } else {
            this.state = state;
        }
        id = -1;
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public String getGuruData() {
        return guruData;
    }

    public void setGuruData(final String guruData) {
        this.guruData = guruData;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof NetworkVO)) {
            return false;
        }
        final NetworkVO that = (NetworkVO) obj;
        if (trafficType != that.trafficType) {
            return false;
        }

        if ((cidr == null && that.cidr != null) || (cidr != null && that.cidr == null)) {
            return false;
        }

        if (cidr == null && that.cidr == null) {
            return true;
        }

        return NetUtils.isNetworkAWithinNetworkB(cidr, that.cidr);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("Ntwk[");
        buf.append(id).append("|").append(trafficType).append("|").append(networkOfferingId).append("]");
        return buf.toString();
    }

    public String getDns1() {
        return dns1;
    }

    public void setDns1(final String dns) {
        dns1 = dns;
    }

    public String getDns2() {
        return dns2;
    }

    public void setDns2(final String dns) {
        dns2 = dns;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    public void setMode(final Mode mode) {
        this.mode = mode;
    }

    @Override
    public BroadcastDomainType getBroadcastDomainType() {
        return broadcastDomainType;
    }

    public void setBroadcastDomainType(final BroadcastDomainType broadcastDomainType) {
        this.broadcastDomainType = broadcastDomainType;
    }

    @Override
    public TrafficType getTrafficType() {
        return trafficType;
    }

    @Override
    public void setTrafficType(final TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    // "cidr" is the Cloudstack managed address space, all CloudStack managed vms get IP address from "cidr"
    // In general "cidr" also serves as the network cidr
    // But in case IP reservation feature is configured for a Guest network, "network_cidr" is the Effective network cidr for the network,
    //  "cidr" will still continue to be the effective address space for CloudStack managed vms in that Guest network
    @Override
    public String getCidr() {
        return cidr;
    }

    public void setCidr(final String cidr) {
        this.cidr = cidr;
    }

    // "networkcidr" is the network CIDR of the guest network which is configured with IP reservation feature
    // It is the summation of "cidr" and the reservedIPrange(the address space used for non cloudstack purposes.)
    //  For networks not using IP reservation "networkcidr" is always null
    @Override
    public String getNetworkCidr() {
        return networkCidr;
    }

    public void setNetworkCidr(final String networkCidr) {
        this.networkCidr = networkCidr;
    }

    @Override
    public String getIp6Gateway() {
        return ip6Gateway;
    }

    @Override
    public String getIp6Cidr() {
        return ip6Cidr;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public long getNetworkOfferingId() {
        return networkOfferingId;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean isRedundant() {
        return this.isRedundant;
    }

    @Override
    public long getRelated() {
        return related;
    }

    @Override
    public URI getBroadcastUri() {
        return broadcastUri;
    }

    public void setBroadcastUri(final URI broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(final String reservationId) {
        this.reservationId = reservationId;
    }

    @Override
    public String getNetworkDomain() {
        return networkDomain;
    }

    public void setNetworkDomain(final String networkDomain) {
        this.networkDomain = networkDomain;
    }

    @Override
    public Network.GuestType getGuestType() {
        return guestType;
    }

    @Override
    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    @Override
    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    @Override
    public ControlledEntity.ACLType getAclType() {
        return aclType;
    }

    @Override
    public boolean isRestartRequired() {
        return restartRequired;
    }

    public void setRestartRequired(final boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

    @Override
    public boolean getSpecifyIpRanges() {
        return specifyIpRanges;
    }

    @Override()
    public boolean getDisplayNetwork() {
        return displayNetwork;
    }

    public void setDisplayNetwork(final boolean displayNetwork) {
        this.displayNetwork = displayNetwork;
    }

    @Override
    public boolean isDisplay() {
        return displayNetwork;
    }

    @Override
    public String getGuruName() {
        return guruName;
    }

    public void setGuruName(final String guruName) {
        this.guruName = guruName;
    }

    @Override
    public Long getVpcId() {
        return vpcId;
    }

    @Override
    public Long getNetworkACLId() {
        return networkACLId;
    }

    @Override
    public void setNetworkACLId(final Long networkACLId) {
        this.networkACLId = networkACLId;
    }

    @Override
    public boolean isStrechedL2Network() {
        return strechedL2Network;
    }

    public void setStrechedL2Network(final boolean strechedL2Network) {
        this.strechedL2Network = strechedL2Network;
    }

    public void setVpcId(final Long vpcId) {
        this.vpcId = vpcId;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    // don't use this directly when possible, use Network state machine instead
    public void setState(final State state) {
        this.state = state;
    }

    public void setNetworkOfferingId(final long networkOfferingId) {
        this.networkOfferingId = networkOfferingId;
    }

    public void setIp6Cidr(final String ip6Cidr) {
        this.ip6Cidr = ip6Cidr;
    }

    public void setIp6Gateway(final String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return Network.class;
    }

    public void setIsReduntant(final boolean reduntant) {
        this.isRedundant = reduntant;
    }
}
