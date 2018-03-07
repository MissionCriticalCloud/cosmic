package com.cloud.agent.api.to.overviews;

import com.cloud.network.Network;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class NetworkOverviewTO {
    private InterfaceTO[] interfaces;
    private ServiceTO services;
    private RouteTO[] routes;
    private VPNTO vpn;
    private SyslogTO syslog;

    public InterfaceTO[] getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(final InterfaceTO[] interfaces) {
        this.interfaces = interfaces;
    }

    public ServiceTO getServices() {
        return services;
    }

    public void setServices(final ServiceTO services) {
        this.services = services;
    }

    public RouteTO[] getRoutes() {
        return routes;
    }

    public void setRoutes(final RouteTO[] routes) {
        this.routes = routes;
    }

    public VPNTO getVpn() {
        return vpn;
    }

    public void setVpn(final VPNTO vpn) {
        this.vpn = vpn;
    }

    public SyslogTO getSyslog() {
        return syslog;
    }

    public void setSyslog(final SyslogTO syslog) {
        this.syslog = syslog;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetworkOverviewTO)) {
            return false;
        }
        final NetworkOverviewTO that = (NetworkOverviewTO) o;
        return Arrays.equals(getInterfaces(), that.getInterfaces()) &&
                Objects.equals(getServices(), that.getServices()) &&
                Arrays.equals(getRoutes(), that.getRoutes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInterfaces(), getServices(), getRoutes());
    }

    public static class InterfaceTO {
        private String macAddress;
        private IPv4AddressTO[] ipv4Addresses;
        private MetadataTO metadata;

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(final String macAddress) {
            this.macAddress = macAddress;
        }

        public IPv4AddressTO[] getIpv4Addresses() {
            return ipv4Addresses;
        }

        public void setIpv4Addresses(final IPv4AddressTO[] ipv4Addresses) {
            this.ipv4Addresses = ipv4Addresses;
        }

        public MetadataTO getMetadata() {
            return metadata;
        }

        public void setMetadata(final MetadataTO metadata) {
            this.metadata = metadata;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof InterfaceTO)) {
                return false;
            }
            final InterfaceTO that = (InterfaceTO) o;
            return Objects.equals(getMacAddress(), that.getMacAddress()) &&
                    Arrays.equals(getIpv4Addresses(), that.getIpv4Addresses()) &&
                    Objects.equals(getMetadata(), that.getMetadata());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMacAddress(), getIpv4Addresses(), getMetadata());
        }

        public static class MetadataTO {
            private String type;
            private String domainName;
            private String dns1;
            private String dns2;

            public MetadataTO() {
            }

            public MetadataTO(Network network) {
                final TrafficType trafficType = network.getTrafficType();
                final GuestType guestType = network.getGuestType();

                if (TrafficType.Public.equals(trafficType)) {
                    type = "public";
                } else if (TrafficType.Guest.equals(trafficType) && GuestType.Isolated.equals(guestType)) {
                    type = "guesttier";
                } else if (TrafficType.Guest.equals(trafficType) && GuestType.Private.equals(guestType)) {
                    type = "private";
                } else if (TrafficType.Guest.equals(trafficType) && GuestType.Sync.equals(guestType)) {
                    type = "sync";
                } else {
                    type = "other";
                }

                if (StringUtils.isNotBlank(network.getNetworkDomain())) {
                    domainName = network.getNetworkDomain();
                }

                if (StringUtils.isNotBlank(network.getDns1())) {
                    dns1 = network.getDns1();
                } else {
                    dns1 = network.getGateway();
                }

                if (StringUtils.isNotBlank(network.getDns2())) {
                    dns2 = network.getDns2();
                }
            }

            public String getType() {
                return type;
            }

            public void setType(final String type) {
                this.type = type;
            }

            public String getDomainName() {
                return domainName;
            }

            public void setDomainName(final String domainName) {
                this.domainName = domainName;
            }

            public String getDns1() {
                return dns1;
            }

            public void setDns1(final String dns1) {
                this.dns1 = dns1;
            }

            public String getDns2() {
                return dns2;
            }

            public void setDns2(final String dns2) {
                this.dns2 = dns2;
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof MetadataTO)) {
                    return false;
                }
                final MetadataTO that = (MetadataTO) o;
                return Objects.equals(getType(), that.getType()) &&
                        Objects.equals(getDomainName(), that.getDomainName()) &&
                        Objects.equals(getDns1(), that.getDns1()) &&
                        Objects.equals(getDns2(), that.getDns2());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getType(), getDomainName(), getDns1(), getDns2());
            }
        }

        public static class IPv4AddressTO {
            private String cidr;
            private String gateway;
            private LoadBalancingRuleTO[] loadBalancingRules;

            public IPv4AddressTO() {
            }

            public IPv4AddressTO(final String cidr, final String gateway) {
                this.cidr = cidr;
                this.gateway = gateway;
            }

            public String getCidr() {
                return cidr;
            }

            public void setCidr(final String cidr) {
                this.cidr = cidr;
            }

            public String getGateway() {
                return gateway;
            }

            public void setGateway(final String gateway) {
                this.gateway = gateway;
            }

            public LoadBalancingRuleTO[] getLoadBalancingRules() {
                return loadBalancingRules;
            }

            public void setLoadBalancingRules(final LoadBalancingRuleTO[] loadBalancingRules) {
                this.loadBalancingRules = loadBalancingRules;
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof IPv4AddressTO)) {
                    return false;
                }
                final IPv4AddressTO that = (IPv4AddressTO) o;
                return Objects.equals(getCidr(), that.getCidr()) &&
                        Objects.equals(getGateway(), that.getGateway());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getCidr(), getGateway());
            }

            public static class LoadBalancingRuleTO {
                private String method;
                private String protocol;
                private String scheduler;
                private long port;

                private RealServerTO[] realServers;

                public String getMethod() {
                    return method;
                }

                public void setMethod(final String method) {
                    this.method = method;
                }

                public String getProtocol() {
                    return protocol;
                }

                public void setProtocol(final String protocol) {
                    this.protocol = protocol;
                }

                public String getScheduler() {
                    return scheduler;
                }

                public void setScheduler(final String scheduler) {
                    this.scheduler = scheduler;
                }

                public long getPort() {
                    return port;
                }

                public void setPort(final long port) {
                    this.port = port;
                }

                public RealServerTO[] getRealServers() {
                    return realServers;
                }

                public void setRealServers(final RealServerTO[] realServers) {
                    this.realServers = realServers;
                }

                @Override
                public boolean equals(final Object o) {
                    if (this == o) {
                        return true;
                    }
                    if (o == null || getClass() != o.getClass()) {
                        return false;
                    }
                    final LoadBalancingRuleTO that = (LoadBalancingRuleTO) o;
                    return Objects.equals(method, that.method) &&
                            Objects.equals(protocol, that.protocol) &&
                            Objects.equals(scheduler, that.scheduler) &&
                            Objects.equals(port, that.port);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(method, protocol, scheduler, port);
                }

                public static class RealServerTO {
                    private String ipv4Address;
                    private long weight;

                    public RealServerTO(final String ipv4Address, final int weight) {
                        this.ipv4Address = ipv4Address;
                        this.weight = weight;
                    }

                    public String getIpv4Address() {
                        return ipv4Address;
                    }

                    public void setIpv4Address(final String ipv4Address) {
                        this.ipv4Address = ipv4Address;
                    }

                    public long getWeight() {
                        return weight;
                    }

                    public void setWeight(final long weight) {
                        this.weight = weight;
                    }
                }
            }
        }
    }

    public static class ServiceTO {
        private ServiceSourceNatTO[] sourceNat;

        public ServiceSourceNatTO[] getSourceNat() {
            return sourceNat;
        }

        public void setSourceNat(final ServiceSourceNatTO[] sourceNat) {
            this.sourceNat = sourceNat;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ServiceTO)) {
                return false;
            }
            final ServiceTO serviceTO = (ServiceTO) o;
            return Arrays.equals(getSourceNat(), serviceTO.getSourceNat());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(getSourceNat());
        }

        public static class ServiceSourceNatTO {
            private String to;
            private String gateway;

            public ServiceSourceNatTO() {
            }

            public ServiceSourceNatTO(final String to, final String gateway) {
                this.to = to;
                this.gateway = gateway;
            }

            public String getTo() {
                return to;
            }

            public void setTo(final String to) {
                this.to = to;
            }

            public String getGateway() {
                return gateway;
            }

            public void setGateway(final String gateway) {
                this.gateway = gateway;
            }

            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof ServiceSourceNatTO)) {
                    return false;
                }
                final ServiceSourceNatTO that = (ServiceSourceNatTO) o;
                return Objects.equals(getTo(), that.getTo()) &&
                        Objects.equals(getGateway(), that.getGateway());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getTo(), getGateway());
            }
        }
    }

    public static class RouteTO {
        private String cidr;
        private String nextHop;
        private Integer metric;

        public RouteTO() {
        }

        public RouteTO(final String cidr, final String nextHop, final Integer metric) {
            this.cidr = cidr;
            this.nextHop = nextHop;
            this.metric = metric;
        }

        public String getCidr() {
            return cidr;
        }

        public void setCidr(final String cidr) {
            this.cidr = cidr;
        }

        public String getNextHop() {
            return nextHop;
        }

        public void setNextHop(final String nextHop) {
            this.nextHop = nextHop;
        }

        public Integer getMetric() {
            return metric;
        }

        public void setMetric(final Integer metric) {
            this.metric = metric;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RouteTO)) {
                return false;
            }
            final RouteTO routeTO = (RouteTO) o;
            return Objects.equals(getCidr(), routeTO.getCidr()) &&
                    Objects.equals(getNextHop(), routeTO.getNextHop()) &&
                    Objects.equals(getMetric(), routeTO.getMetric());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getCidr(), getNextHop(), getMetric());
        }
    }

    public static class VPNTO {
        private RemoteAccessTO remoteAccess;
        private Site2SiteTO[] site2site;

        public RemoteAccessTO getRemoteAccess() {
            return remoteAccess;
        }

        public void setRemoteAccess(final RemoteAccessTO remoteAccess) {
            this.remoteAccess = remoteAccess;
        }

        public Site2SiteTO[] getSite2site() {
            return site2site;
        }

        public void setSite2site(final Site2SiteTO[] site2site) {
            this.site2site = site2site;
        }

        public static class RemoteAccessTO {
            private String ipRange;
            private String localCidr;
            private String localIp;
            private String preSharedKey;
            private String vpnServerIp;
            private VPNUserTO[] vpnUsers;

            public String getIpRange() {
                return ipRange;
            }

            public void setIpRange(final String ipRange) {
                this.ipRange = ipRange;
            }

            public String getLocalCidr() {
                return localCidr;
            }

            public void setLocalCidr(final String localCidr) {
                this.localCidr = localCidr;
            }

            public String getLocalIp() {
                return localIp;
            }

            public void setLocalIp(final String localIp) {
                this.localIp = localIp;
            }

            public String getPreSharedKey() {
                return preSharedKey;
            }

            public void setPreSharedKey(final String preSharedKey) {
                this.preSharedKey = preSharedKey;
            }

            public String getVpnServerIp() {
                return vpnServerIp;
            }

            public void setVpnServerIp(final String vpnServerIp) {
                this.vpnServerIp = vpnServerIp;
            }

            public VPNUserTO[] getVpnUsers() {
                return vpnUsers;
            }

            public void setVpnUsers(final VPNUserTO[] vpnUsers) {
                this.vpnUsers = vpnUsers;
            }

            public static class VPNUserTO {
                private String username;
                private String password;

                public VPNUserTO() {
                }

                public VPNUserTO(final String username, final String password) {
                    this.username = username;
                    this.password = password;
                }

                public String getUsername() {
                    return username;
                }

                public void setUsername(final String username) {
                    this.username = username;
                }

                public String getPassword() {
                    return password;
                }

                public void setPassword(final String password) {
                    this.password = password;
                }
            }
        }

        public static class Site2SiteTO {
            private Boolean dpd;
            private Boolean forceEncaps;
            private Long lifetime;
            private String esp;
            private Long ikeLifetime;
            private String ike;
            private String psk;

            private String left;
            private String leftSubnet;

            private Boolean passive;

            private String right;
            private String peerList;

            public Boolean getDpd() {
                return dpd;
            }

            public void setDpd(final Boolean dpd) {
                this.dpd = dpd;
            }

            public Boolean getForceEncaps() {
                return forceEncaps;
            }

            public void setForceEncaps(final Boolean forceEncaps) {
                this.forceEncaps = forceEncaps;
            }

            public Long getLifetime() {
                return lifetime;
            }

            public void setLifetime(final Long lifetime) {
                this.lifetime = lifetime;
            }

            public String getEsp() {
                return esp;
            }

            public void setEsp(final String esp) {
                this.esp = esp;
            }

            public Long getIkeLifetime() {
                return ikeLifetime;
            }

            public void setIkeLifetime(final Long ikeLifetime) {
                this.ikeLifetime = ikeLifetime;
            }

            public String getIke() {
                return ike;
            }

            public void setIke(final String ike) {
                this.ike = ike;
            }

            public String getPsk() {
                return psk;
            }

            public void setPsk(final String psk) {
                this.psk = psk;
            }

            public String getLeft() {
                return left;
            }

            public void setLeft(final String left) {
                this.left = left;
            }

            public String getLeftSubnet() {
                return leftSubnet;
            }

            public void setLeftSubnet(final String leftSubnet) {
                this.leftSubnet = leftSubnet;
            }

            public Boolean getPassive() {
                return passive;
            }

            public void setPassive(final Boolean passive) {
                this.passive = passive;
            }

            public String getRight() {
                return right;
            }

            public void setRight(final String right) {
                this.right = right;
            }

            public String getPeerList() {
                return peerList;
            }

            public void setPeerList(final String peerList) {
                this.peerList = peerList;
            }
        }
    }

    public static class SyslogTO {
        private String[] servers;

        public String[] getServers() {
            return servers;
        }

        public void setServers(final String[] servers) {
            this.servers = servers;
        }
    }
}
