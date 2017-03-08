package com.cloud.db.model;

import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.NetworkType;

import javax.persistence.Access;
import javax.persistence.AccessType;
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

@Entity
@Table(name = "data_center")
public class Zone extends com.cloud.model.Zone {

    @Access(AccessType.PROPERTY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public long getId() {
        return super.getId();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "allocation_state")
    @Enumerated(value = EnumType.STRING)
    public AllocationState getAllocationState() {
        return super.getAllocationState();
    }

    @Transient
    public Map<String, String> getDetails() {
        return super.getDetails();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "description")
    public String getDescription() {
        return super.getDescription();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "dhcp_provider")
    public String getDhcpProvider() {
        return super.getDhcpProvider();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "dns1")
    public String getDns1() {
        return super.getDns1();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "dns2")
    public String getDns2() {
        return super.getDns2();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "dns_provider")
    public String getDnsProvider() {
        return super.getDnsProvider();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "domain_id")
    public Long getDomainId() {
        return super.getDomainId();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "domain")
    public String getDomain() {
        return super.getDomain();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "firewall_provider")
    public String getFirewallProvider() {
        return super.getFirewallProvider();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "gateway_provider")
    public String getGatewayProvider() {
        return super.getGatewayProvider();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "guest_network_cidr")
    public String getGuestNetworkCidr() {
        return super.getGuestNetworkCidr();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "internal_dns1")
    public String getInternalDns1() {
        return super.getInternalDns1();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "internal_dns2")
    public String getInternalDns2() {
        return super.getInternalDns2();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "ip6_dns1")
    public String getIp6Dns1() {
        return super.getIp6Dns1();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "ip6_dns2")
    public String getIp6Dns2() {
        return super.getIp6Dns2();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "lb_provider")
    public String getLoadBalancerProvider() {
        return super.getLoadBalancerProvider();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "is_local_storage_enabled")
    public boolean isLocalStorageEnabled() {
        return super.isLocalStorageEnabled();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "mac_address", nullable = false)
    @TableGenerator(name = "mac_address_sq", table = "data_center", pkColumnName = "id", valueColumnName = "mac_address", allocationSize = 1)
    public long getMacAddress() {
        return super.getMacAddress();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "name")
    public String getName() {
        return super.getName();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "networktype")
    @Enumerated(EnumType.STRING)
    public NetworkType getNetworkType() {
        return super.getNetworkType();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "removed")
    public Date getRemoved() {
        return super.getRemoved();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "router_mac_address", updatable = false, nullable = false)
    public String getRouterMacAddress() {
        return super.getRouterMacAddress();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "is_security_group_enabled")
    public boolean isSecurityGroupEnabled() {
        return super.isSecurityGroupEnabled();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "userdata_provider")
    public String getUserDataProvider() {
        return super.getUserDataProvider();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "uuid")
    public String getUuid() {
        return super.getUuid();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "vpn_provider")
    public String getVpnProvider() {
        return super.getVpnProvider();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "zone_token")
    public String getZoneToken() {
        return super.getZoneToken();
    }
}
