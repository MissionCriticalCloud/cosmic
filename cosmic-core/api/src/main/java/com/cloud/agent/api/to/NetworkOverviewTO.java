package com.cloud.agent.api.to;

import com.cloud.network.Network;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.StringUtils;

public class NetworkOverviewTO {
    private InterfaceTO[] interfaces;
    private ServiceTO services;

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

    public static class InterfaceTO {
        private String macAddress;
        private String[] ipv4Addresses;
        private MetadataTO metadata;

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(final String macAddress) {
            this.macAddress = macAddress;
        }

        public String[] getIpv4Addresses() {
            return ipv4Addresses;
        }

        public void setIpv4Addresses(final String[] ipv4Addresses) {
            this.ipv4Addresses = ipv4Addresses;
        }

        public MetadataTO getMetadata() {
            return metadata;
        }

        public void setMetadata(final MetadataTO metadata) {
            this.metadata = metadata;
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
                    type = "tier";
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
        }
    }

    public static class ServiceTO {
        private ServiceSourceNatTO[] sourceNat;

        public ServiceTO() {
        }

        public static class ServiceSourceNatTO {
            private String to;

            public ServiceSourceNatTO() {
            }

            public ServiceSourceNatTO(final String to) {
                this.to = to;
            }

            public String getTo() {
                return to;
            }

            public void setTo(final String to) {
                this.to = to;
            }
        }

        public ServiceSourceNatTO[] getSourceNat() {
            return sourceNat;
        }

        public void setSourceNat(final ServiceSourceNatTO[] sourceNat) {
            this.sourceNat = sourceNat;
        }
    }
}
