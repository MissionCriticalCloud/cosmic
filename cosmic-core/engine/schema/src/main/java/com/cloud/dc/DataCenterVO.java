package com.cloud.dc;

import com.cloud.network.Network.Provider;
import com.cloud.org.Grouping;
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
    @Column(name = "is_security_group_enabled")
    boolean securityGroupEnabled;
    @Column(name = "is_local_storage_enabled")
    boolean localStorageEnabled;
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
    @Column(name = "router_mac_address", updatable = false, nullable = false)
    private String routerMacAddress = "02:00:00:00:00:01";
    @Column(name = "guest_network_cidr")
    private String guestNetworkCidr = null;
    @Column(name = "domain_id")
    private Long domainId = null;
    @Column(name = "domain")
    private String domain;
    @Column(name = "dns_provider")
    private String dnsProvider;
    @Column(name = "dhcp_provider")
    private String dhcpProvider;
    @Column(name = "gateway_provider")
    private String gatewayProvider;
    @Column(name = "vpn_provider")
    private String vpnProvider;
    @Column(name = "userdata_provider")
    private String userDataProvider;
    @Column(name = "lb_provider")
    private String loadBalancerProvider;
    @Column(name = "firewall_provider")
    private String firewallProvider;
    @Column(name = "mac_address", nullable = false)
    @TableGenerator(name = "mac_address_sq", table = "data_center", pkColumnName = "id", valueColumnName = "mac_address", allocationSize = 1)
    private long macAddress = 1;
    @Column(name = "zone_token")
    private String zoneToken;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;
    @Column(name = "uuid")
    private String uuid;

    public DataCenterVO(final long id, final String name, final String description, final String dns1, final String dns2, final String dns3, final String dns4, final String
            guestCidr, final String domain, final Long domainId,
                        final NetworkType zoneType, final String zoneToken, final String domainSuffix) {
        this(name, description, dns1, dns2, dns3, dns4, guestCidr, domain, domainId, zoneType, zoneToken, domainSuffix, false, false, null, null);
        this.id = id;
        this.allocationState = Grouping.AllocationState.Enabled;
        this.uuid = UUID.randomUUID().toString();
    }

    public DataCenterVO(final String name, final String description, final String dns1, final String dns2, final String dns3, final String dns4, final String guestCidr, final
    String domain, final Long domainId,
                        final NetworkType zoneType, final String zoneToken, final String domainSuffix, final boolean securityGroupEnabled, final boolean localStorageEnabled,
                        final String ip6Dns1, final String ip6Dns2) {
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
        this.allocationState = Grouping.AllocationState.Enabled;
        this.securityGroupEnabled = securityGroupEnabled;
        this.localStorageEnabled = localStorageEnabled;

        if (zoneType == NetworkType.Advanced) {
            loadBalancerProvider = Provider.VirtualRouter.getName();
            firewallProvider = Provider.VirtualRouter.getName();
            dhcpProvider = Provider.VirtualRouter.getName();
            dnsProvider = Provider.VirtualRouter.getName();
            gatewayProvider = Provider.VirtualRouter.getName();
            vpnProvider = Provider.VirtualRouter.getName();
            userDataProvider = Provider.VirtualRouter.getName();
        } else if (zoneType == NetworkType.Basic) {
            dhcpProvider = Provider.VirtualRouter.getName();
            dnsProvider = Provider.VirtualRouter.getName();
            userDataProvider = Provider.VirtualRouter.getName();
            loadBalancerProvider = Provider.ElasticLoadBalancerVm.getName();
        }

        this.zoneToken = zoneToken;
        this.domain = domainSuffix;
        this.uuid = UUID.randomUUID().toString();
    }

    public DataCenterVO() {
    }

    public String getRouterMacAddress() {
        return routerMacAddress;
    }

    public void setRouterMacAddress(final String routerMacAddress) {
        this.routerMacAddress = routerMacAddress;
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
    public String getDnsProvider() {
        return dnsProvider;
    }

    public void setDnsProvider(final String dnsProvider) {
        this.dnsProvider = dnsProvider;
    }

    @Override
    public String getGatewayProvider() {
        return gatewayProvider;
    }

    public void setGatewayProvider(final String gatewayProvider) {
        this.gatewayProvider = gatewayProvider;
    }

    @Override
    public String getFirewallProvider() {
        return firewallProvider;
    }

    @Override
    public String getDhcpProvider() {
        return dhcpProvider;
    }

    public void setDhcpProvider(final String dhcpProvider) {
        this.dhcpProvider = dhcpProvider;
    }

    @Override
    public String getLoadBalancerProvider() {
        return loadBalancerProvider;
    }

    public void setLoadBalancerProvider(final String loadBalancerProvider) {
        this.loadBalancerProvider = loadBalancerProvider;
    }

    @Override
    public String getUserDataProvider() {
        return userDataProvider;
    }

    @Override
    public String getVpnProvider() {
        return vpnProvider;
    }

    public void setVpnProvider(final String vpnProvider) {
        this.vpnProvider = vpnProvider;
    }

    @Override
    public boolean isSecurityGroupEnabled() {
        return securityGroupEnabled;
    }

    public void setSecurityGroupEnabled(final boolean enabled) {
        this.securityGroupEnabled = enabled;
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

    @Override
    public boolean isLocalStorageEnabled() {
        return localStorageEnabled;
    }

    public void setLocalStorageEnabled(final boolean enabled) {
        this.localStorageEnabled = enabled;
    }

    public void setZoneToken(final String zoneToken) {
        this.zoneToken = zoneToken;
    }

    public void setUserDataProvider(final String userDataProvider) {
        this.userDataProvider = userDataProvider;
    }

    public void setFirewallProvider(final String firewallProvider) {
        this.firewallProvider = firewallProvider;
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
