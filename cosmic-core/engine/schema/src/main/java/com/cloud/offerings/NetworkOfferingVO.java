package com.cloud.offerings;

import com.cloud.network.Network;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.NetworkOffering;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "network_offerings")
public class NetworkOfferingVO implements NetworkOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;
    @Column(name = "display_text")
    private String displayText;
    @Column(name = "nw_rate")
    private Integer rateMbps;
    @Column(name = "mc_rate")
    private Integer multicastRateMbps;
    @Column(name = "traffic_type")
    @Enumerated(value = EnumType.STRING)
    private TrafficType trafficType;
    @Column(name = "specify_vlan")
    private boolean specifyVlan;
    @Column(name = "system_only")
    private boolean systemOnly;
    @Column(name = "service_offering_id")
    private Long serviceOfferingId;
    @Column(name = "secondary_service_offering_id")
    private Long secondaryServiceOfferingId;
    @Column(name = "tags", length = 4096)
    private String tags;
    @Column(name = "default")
    private boolean isDefault;
    @Column(name = "availability")
    @Enumerated(value = EnumType.STRING)
    private Availability availability;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private State state = State.Disabled;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;
    @Column(name = "guest_type")
    @Enumerated(value = EnumType.STRING)
    private Network.GuestType guestType;
    @Column(name = "dedicated_lb_service")
    private boolean dedicatedLB;
    @Column(name = "shared_source_nat_service")
    private boolean sharedSourceNat;
    @Column(name = "specify_ip_ranges")
    private boolean specifyIpRanges = false;
    @Column(name = "sort_key")
    private int sortKey;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "redundant_router_service")
    private boolean redundantRouter;
    @Column(name = "conserve_mode")
    private boolean conserveMode;
    @Column(name = "elastic_ip_service")
    private boolean elasticIp;
    @Column(name = "eip_associate_public_ip")
    private boolean eipAssociatePublicIp;
    @Column(name = "elastic_lb_service")
    private boolean elasticLb;
    @Column(name = "inline")
    private boolean inline;
    @Column(name = "is_persistent")
    private boolean isPersistent;
    @Column(name = "egress_default_policy")
    private boolean egressdefaultpolicy;
    @Column(name = "concurrent_connections")
    private Integer concurrentConnections;
    @Column(name = "keep_alive_enabled")
    private boolean keepAliveEnabled = false;
    @Column(name = "supports_streched_l2")
    private boolean supportsStrechedL2 = false;
    @Column(name = "public_lb")
    private boolean publicLb;
    @Column(name = "unique_name")
    private String uniqueName;

    public NetworkOfferingVO(final String name, final String displayText, final TrafficType trafficType, final boolean systemOnly, final boolean specifyVlan,
                             final Integer rateMbps, final Integer multicastRateMbps, final boolean isDefault, final Availability availability, final String tags,
                             final Network.GuestType guestType, final boolean conserveMode, final boolean dedicatedLb, final boolean sharedSourceNat, final boolean redundantRouter,
                             final boolean elasticIp, final boolean elasticLb, final boolean specifyIpRanges, final boolean inline, final boolean isPersistent,
                             final boolean associatePublicIP, final boolean publicLb, final boolean egressdefaultpolicy,
                             final boolean supportsStrechedL2) {
        this(
                name,
                displayText,
                trafficType,
                systemOnly,
                specifyVlan,
                rateMbps,
                multicastRateMbps,
                isDefault,
                availability,
                tags,
                guestType,
                conserveMode,
                specifyIpRanges,
                isPersistent,
                publicLb
        );
        this.dedicatedLB = dedicatedLb;
        this.sharedSourceNat = sharedSourceNat;
        this.redundantRouter = redundantRouter;
        this.elasticIp = elasticIp;
        this.elasticLb = elasticLb;
        this.inline = inline;
        this.eipAssociatePublicIp = associatePublicIP;
        this.egressdefaultpolicy = egressdefaultpolicy;
        this.supportsStrechedL2 = supportsStrechedL2;
    }

    public NetworkOfferingVO(final String name, final String displayText, final TrafficType trafficType, final boolean systemOnly, final boolean specifyVlan,
                             final Integer rateMbps, final Integer multicastRateMbps, final boolean isDefault, final Availability availability, final String tags,
                             final Network.GuestType guestType, final boolean conserveMode, final boolean specifyIpRanges, final boolean isPersistent,
                             final boolean publicLb) {
        this.name = name;
        this.displayText = displayText;
        this.rateMbps = rateMbps;
        this.multicastRateMbps = multicastRateMbps;
        this.trafficType = trafficType;
        this.systemOnly = systemOnly;
        this.specifyVlan = specifyVlan;
        this.isDefault = isDefault;
        this.availability = availability;
        this.uniqueName = name;
        this.uuid = UUID.randomUUID().toString();
        this.tags = tags;
        this.guestType = guestType;
        this.conserveMode = conserveMode;
        this.dedicatedLB = true;
        this.sharedSourceNat = false;
        this.redundantRouter = false;
        this.elasticIp = false;
        this.eipAssociatePublicIp = true;
        this.elasticLb = false;
        this.inline = false;
        this.specifyIpRanges = specifyIpRanges;
        this.isPersistent = isPersistent;
        this.publicLb = publicLb;
    }

    public NetworkOfferingVO() {
    }

    /**
     * Network Offering for all system vms.
     *
     * @param name
     * @param trafficType
     * @param specifyIpRanges TODO
     */
    public NetworkOfferingVO(final String name, final TrafficType trafficType, final boolean specifyIpRanges) {
        this(name, "System Offering for " + name, trafficType, true, false, 0, 0, true, Availability.Required, null, null, true, specifyIpRanges, false, false);
        this.state = State.Enabled;
    }

    public NetworkOfferingVO(final String name, final Network.GuestType guestType, final boolean specifyVlan) {
        this(
                name,
                "System Offering for " + name,
                TrafficType.Guest,
                false,
                specifyVlan,
                0,
                0,
                true,
                Availability.Optional,
                null,
                guestType,
                true,
                false,
                true,
                false
        );
        this.state = State.Enabled;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public Integer getRateMbps() {
        return rateMbps;
    }

    @Override
    public Integer getMulticastRateMbps() {
        return multicastRateMbps;
    }

    @Override
    public TrafficType getTrafficType() {
        return trafficType;
    }

    @Override
    public boolean getSpecifyVlan() {
        return specifyVlan;
    }

    @Override
    public String getTags() {
        return tags;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean isSystemOnly() {
        return systemOnly;
    }

    @Override
    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(final Availability availability) {
        this.availability = availability;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public Network.GuestType getGuestType() {
        return guestType;
    }

    @Override
    public Long getServiceOfferingId() {
        return serviceOfferingId;
    }

    public void setServiceOfferingId(final Long serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    @Override
    public boolean getDedicatedLB() {
        return dedicatedLB;
    }

    public void setDedicatedLB(final boolean dedicatedLB) {
        this.dedicatedLB = dedicatedLB;
    }

    @Override
    public boolean getSharedSourceNat() {
        return sharedSourceNat;
    }

    public void setSharedSourceNat(final boolean sharedSourceNat) {
        this.sharedSourceNat = sharedSourceNat;
    }

    @Override
    public boolean getRedundantRouter() {
        return redundantRouter;
    }

    public void setRedundantRouter(final boolean redundantRouter) {
        this.redundantRouter = redundantRouter;
    }

    @Override
    public boolean isConserveMode() {
        return conserveMode;
    }

    @Override
    public boolean getElasticIp() {
        return elasticIp;
    }

    @Override
    public boolean getAssociatePublicIP() {
        return eipAssociatePublicIp;
    }

    @Override
    public boolean getElasticLb() {
        return elasticLb;
    }

    @Override
    public boolean getSpecifyIpRanges() {
        return specifyIpRanges;
    }

    @Override
    public boolean isInline() {
        return inline;
    }

    @Override
    public boolean getIsPersistent() {
        return isPersistent;
    }

    public void setIsPersistent(final Boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    @Override
    public boolean getPublicLb() {
        return publicLb;
    }

    @Override
    public boolean getEgressDefaultPolicy() {
        return egressdefaultpolicy;
    }

    public void setEgressdefaultpolicy(final boolean egressdefaultpolicy) {
        this.egressdefaultpolicy = egressdefaultpolicy;
    }

    @Override
    public Integer getConcurrentConnections() {
        return this.concurrentConnections;
    }

    @Override
    public boolean isKeepAliveEnabled() {
        return keepAliveEnabled;
    }

    public void setKeepAliveEnabled(final boolean keepAliveEnabled) {
        this.keepAliveEnabled = keepAliveEnabled;
    }

    @Override
    public boolean getSupportsStrechedL2() {
        return supportsStrechedL2;
    }

    public void setConcurrentConnections(final Integer concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    public void setPublicLb(final boolean publicLb) {
        this.publicLb = publicLb;
    }

    public void setUniqueName(final String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public void setMulticastRateMbps(final Integer multicastRateMbps) {
        this.multicastRateMbps = multicastRateMbps;
    }

    public void setRateMbps(final Integer rateMbps) {
        this.rateMbps = rateMbps;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public Date getRemoved() {
        return removed;
    }

    public Long getSecondaryServiceOfferingId() {
        if (secondaryServiceOfferingId != null) {
            return secondaryServiceOfferingId;
        } else {
            return getServiceOfferingId();
        }
    }

    public void setSecondaryServiceOfferingId(final Long secondaryServiceOfferingId) {
        this.secondaryServiceOfferingId = secondaryServiceOfferingId;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("[Network Offering [");
        return buf.append(id).append("-").append(trafficType).append("-").append(name).append("]").toString();
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public int getSortKey() {
        return sortKey;
    }

    public void setSortKey(final int key) {
        sortKey = key;
    }
}
