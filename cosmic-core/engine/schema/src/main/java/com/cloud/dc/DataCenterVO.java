package com.cloud.dc;

import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "data_center")
public class DataCenterVO implements DataCenter {

    @Column(name = "networktype")
    @Enumerated(EnumType.STRING)
    NetworkType networkType;
    // This is a delayed load value.  If the value is null,
    // then this field has not been loaded yet.
    // Call the dao to load it.
    @Transient
    Map<String, String> details;
    @Column(name = "allocation_state")
    @Enumerated(value = EnumType.STRING)
    AllocationState allocationState;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name = null;
    @Column(name = "description")
    private String description = null;
    @Column(name = "dns1")
    private String dns1 = null;
    @Column(name = "dns2")
    private String dns2 = null;
    @Column(name = "ip6_dns1")
    private String ip6Dns1 = null;
    @Column(name = "ip6_dns2")
    private String ip6Dns2 = null;
    @Column(name = "internal_dns1")
    private String internalDns1 = null;
    @Column(name = "internal_dns2")
    private String internalDns2 = null;
    @Column(name = "guest_network_cidr")
    private String guestNetworkCidr = null;
    @Column(name = "domain_id")
    private Long domainId = null;
    @Column(name = "domain")
    private String domain;
    @Column(name = "mac_address", nullable = false)
    @TableGenerator(name = "mac_address_sq", table = "data_center", pkColumnName = "id", valueColumnName = "mac_address", allocationSize = 1)
    private long macAddress = 1;
    @Column(name = "zone_token")
    private String zoneToken;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "uuid")
    private String uuid;

    public DataCenterVO(final long id, final String name, final String description, final String dns1, final String dns2, final String dns3, final String dns4, final String guestCidr, final String
            domain, final Long domainId, final NetworkType zoneType, final String zoneToken, final String domainSuffix) {
        this(name, description, dns1, dns2, dns3, dns4, guestCidr, domain, domainId, zoneType, zoneToken, domainSuffix, null, null);
        this.id = id;
        this.allocationState = AllocationState.Enabled;
        this.uuid = UUID.randomUUID().toString();
    }

    public DataCenterVO(final String name, final String description, final String dns1, final String dns2, final String dns3, final String dns4, final String guestCidr, final
    String domain, final Long domainId, final NetworkType zoneType, final String zoneToken, final String domainSuffix, final String ip6Dns1, final String ip6Dns2) {
        this.name = name;
        this.description = description;
        this.dns1 = dns1;
        this.dns2 = dns2;
        this.ip6Dns1 = ip6Dns1;
        this.ip6Dns2 = ip6Dns2;
        this.internalDns1 = dns3;
        this.internalDns2 = dns4;
        this.guestNetworkCidr = guestCidr;
        this.domain = domain;
        this.domainId = domainId;
        this.networkType = zoneType;
        this.allocationState = AllocationState.Enabled;

        this.zoneToken = zoneToken;
        this.domain = domainSuffix;
        this.uuid = UUID.randomUUID().toString();
    }

    public DataCenterVO() {
    }

    @Override
    public String getDns1() {
        return dns1;
    }

    @Override
    public String getDns2() {
        return dns2;
    }

    public void setDns2(final String dns2) {
        this.dns2 = dns2;
    }

    @Override
    public String getIp6Dns1() {
        return ip6Dns1;
    }

    public void setIp6Dns1(final String ip6Dns1) {
        this.ip6Dns1 = ip6Dns1;
    }

    @Override
    public String getIp6Dns2() {
        return ip6Dns2;
    }

    @Override
    public String getGuestNetworkCidr() {
        return guestNetworkCidr;
    }

    public void setGuestNetworkCidr(final String guestNetworkCidr) {
        this.guestNetworkCidr = guestNetworkCidr;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    @Override
    public NetworkType getNetworkType() {
        return networkType;
    }

    @Override
    public String getInternalDns1() {
        return internalDns1;
    }

    @Override
    public String getInternalDns2() {
        return internalDns2;
    }

    @Override
    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public void setDetails(final Map<String, String> details2) {
        details = details2;
    }

    @Override
    public AllocationState getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final AllocationState allocationState) {
        this.allocationState = allocationState;
    }

    @Override
    public String getZoneToken() {
        return zoneToken;
    }

    public void setInternalDns2(final String dns4) {
        this.internalDns2 = dns4;
    }

    public void setInternalDns1(final String dns3) {
        this.internalDns1 = dns3;
    }

    public void setNetworkType(final NetworkType zoneNetworkType) {
        this.networkType = zoneNetworkType;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setIp6Dns2(final String ip6Dns2) {
        this.ip6Dns2 = ip6Dns2;
    }

    public void setDns1(final String dns1) {
        this.dns1 = dns1;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getDetail(final String name) {
        return details != null ? details.get(name) : null;
    }

    public void setDetail(final String name, final String value) {
        assert (details != null) : "Did you forget to load the details?";

        details.put(name, value);
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DataCenterVO)) {
            return false;
        }
        final DataCenterVO that = (DataCenterVO) obj;
        return this.id == that.id;
    }

    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public long getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final long macAddress) {
        this.macAddress = macAddress;
    }
}
