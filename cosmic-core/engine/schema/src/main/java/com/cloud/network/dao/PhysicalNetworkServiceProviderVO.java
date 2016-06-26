package com.cloud.network.dao;

import com.cloud.network.Network.Service;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "physical_network_service_providers")
public class PhysicalNetworkServiceProviderVO implements PhysicalNetworkServiceProvider, InternalIdentity {
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    State state;
    @Column(name = "vpn_service_provided")
    boolean vpnServiceProvided;
    @Column(name = "dhcp_service_provided")
    boolean dhcpServiceProvided;
    @Column(name = "dns_service_provided")
    boolean dnsServiceProvided;
    @Column(name = "gateway_service_provided")
    boolean gatewayServiceProvided;
    @Column(name = "firewall_service_provided")
    boolean firewallServiceProvided;
    @Column(name = "source_nat_service_provided")
    boolean sourcenatServiceProvided;
    @Column(name = "load_balance_service_provided")
    boolean lbServiceProvided;
    @Column(name = "static_nat_service_provided")
    boolean staticnatServiceProvided;
    @Column(name = "port_forwarding_service_provided")
    boolean portForwardingServiceProvided;
    @Column(name = "user_data_service_provided")
    boolean userdataServiceProvided;
    @Column(name = "security_group_service_provided")
    boolean securitygroupServiceProvided;
    @Column(name = "networkacl_service_provided")
    boolean networkAclServiceProvided;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "physical_network_id")
    private long physicalNetworkId;
    @Column(name = "destination_physical_network_id")
    private long destPhysicalNetworkId;
    @Column(name = "provider_name")
    private String providerName;

    public PhysicalNetworkServiceProviderVO() {
    }

    public PhysicalNetworkServiceProviderVO(final long physicalNetworkId, final String name) {
        this.physicalNetworkId = physicalNetworkId;
        this.providerName = name;
        this.state = State.Disabled;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
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
    public long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public long getDestinationPhysicalNetworkId() {
        return destPhysicalNetworkId;
    }

    public void setDestinationPhysicalNetworkId(final long destPhysicalNetworkId) {
        this.destPhysicalNetworkId = destPhysicalNetworkId;
    }

    @Override
    public boolean isLbServiceProvided() {
        return lbServiceProvided;
    }

    @Override
    public boolean isVpnServiceProvided() {
        return vpnServiceProvided;
    }

    public void setVpnServiceProvided(final boolean vpnServiceProvided) {
        this.vpnServiceProvided = vpnServiceProvided;
    }

    @Override
    public boolean isDhcpServiceProvided() {
        return dhcpServiceProvided;
    }

    public void setDhcpServiceProvided(final boolean dhcpServiceProvided) {
        this.dhcpServiceProvided = dhcpServiceProvided;
    }

    @Override
    public boolean isDnsServiceProvided() {
        return dnsServiceProvided;
    }

    public void setDnsServiceProvided(final boolean dnsServiceProvided) {
        this.dnsServiceProvided = dnsServiceProvided;
    }

    @Override
    public boolean isGatewayServiceProvided() {
        return gatewayServiceProvided;
    }

    public void setGatewayServiceProvided(final boolean gatewayServiceProvided) {
        this.gatewayServiceProvided = gatewayServiceProvided;
    }

    @Override
    public boolean isFirewallServiceProvided() {
        return firewallServiceProvided;
    }

    public void setFirewallServiceProvided(final boolean firewallServiceProvided) {
        this.firewallServiceProvided = firewallServiceProvided;
    }

    @Override
    public boolean isSourcenatServiceProvided() {
        return sourcenatServiceProvided;
    }

    public void setSourcenatServiceProvided(final boolean sourcenatServiceProvided) {
        this.sourcenatServiceProvided = sourcenatServiceProvided;
    }

    @Override
    public boolean isUserdataServiceProvided() {
        return userdataServiceProvided;
    }

    public void setUserdataServiceProvided(final boolean userdataServiceProvided) {
        this.userdataServiceProvided = userdataServiceProvided;
    }

    @Override
    public boolean isSecuritygroupServiceProvided() {
        return securitygroupServiceProvided;
    }

    public void setSecuritygroupServiceProvided(final boolean securitygroupServiceProvided) {
        this.securitygroupServiceProvided = securitygroupServiceProvided;
    }

    @Override
    public List<Service> getEnabledServices() {
        final List<Service> services = new ArrayList<>();
        if (this.isVpnServiceProvided()) {
            services.add(Service.Vpn);
        }
        if (this.isDhcpServiceProvided()) {
            services.add(Service.Dhcp);
        }
        if (this.isDnsServiceProvided()) {
            services.add(Service.Dns);
        }
        if (this.isGatewayServiceProvided()) {
            services.add(Service.Gateway);
        }
        if (this.isFirewallServiceProvided()) {
            services.add(Service.Firewall);
        }
        if (this.isLbServiceProvided()) {
            services.add(Service.Lb);
        }
        if (this.sourcenatServiceProvided) {
            services.add(Service.SourceNat);
        }
        if (this.staticnatServiceProvided) {
            services.add(Service.StaticNat);
        }
        if (this.portForwardingServiceProvided) {
            services.add(Service.PortForwarding);
        }
        if (this.isUserdataServiceProvided()) {
            services.add(Service.UserData);
        }
        if (this.isSecuritygroupServiceProvided()) {
            services.add(Service.SecurityGroup);
        }
        return services;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean isNetworkAclServiceProvided() {
        return networkAclServiceProvided;
    }

    public void setNetworkAclServiceProvided(final boolean networkAclServiceProvided) {
        this.networkAclServiceProvided = networkAclServiceProvided;
    }

    public void setEnabledServices(final List<Service> services) {
        this.setVpnServiceProvided(services.contains(Service.Vpn));
        this.setDhcpServiceProvided(services.contains(Service.Dhcp));
        this.setDnsServiceProvided(services.contains(Service.Dns));
        this.setGatewayServiceProvided(services.contains(Service.Gateway));
        this.setFirewallServiceProvided(services.contains(Service.Firewall));
        this.setLbServiceProvided(services.contains(Service.Lb));
        this.setSourcenatServiceProvided(services.contains(Service.SourceNat));
        this.setStaticnatServiceProvided(services.contains(Service.StaticNat));
        this.setPortForwardingServiceProvided(services.contains(Service.PortForwarding));
        this.setUserdataServiceProvided(services.contains(Service.UserData));
        this.setSecuritygroupServiceProvided(services.contains(Service.SecurityGroup));
        this.setNetworkAclServiceProvided(services.contains(Service.NetworkACL));
    }

    public void setLbServiceProvided(final boolean lbServiceProvided) {
        this.lbServiceProvided = lbServiceProvided;
    }

    public boolean isStaticnatServiceProvided() {
        return staticnatServiceProvided;
    }

    public void setStaticnatServiceProvided(final boolean staticnatServiceProvided) {
        this.staticnatServiceProvided = staticnatServiceProvided;
    }

    public boolean isPortForwardingServiceProvided() {
        return portForwardingServiceProvided;
    }

    public void setPortForwardingServiceProvided(final boolean portForwardingServiceProvided) {
        this.portForwardingServiceProvided = portForwardingServiceProvided;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }
}
