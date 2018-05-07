package com.cloud.agent.resource.kvm;

import java.util.ArrayList;
import java.util.List;

public class LibvirtNetworkDef {
    private final String networkName;
    private final String uuid;
    private final String domainName;
    private final List<IpRange> ipranges = new ArrayList<>();
    private final List<DhcpMapping> dhcpMaps = new ArrayList<>();
    private NetworkType networkType;
    private String bridgeName;
    private boolean stp;
    private int delay;
    private String fwDev;
    private String brIpAddr;
    private String bridgeNetMask;

    public LibvirtNetworkDef(final String networkName, final String uuid, final String domainName) {
        this.networkName = networkName;
        this.uuid = uuid;
        this.domainName = domainName;
    }

    public void defNatNetwork(final String brName, final boolean stp, final int delay, final String fwNic, final String ipAddr, final String netMask) {
        this.networkType = NetworkType.NAT;
        this.bridgeName = brName;
        this.stp = stp;
        this.delay = delay;
        this.fwDev = fwNic;
        this.brIpAddr = ipAddr;
        this.bridgeNetMask = netMask;
    }

    public void defBrNetwork(final String brName, final boolean stp, final int delay, final String fwNic, final String ipAddr, final String netMask) {
        this.networkType = NetworkType.BRIDGE;
        this.bridgeName = brName;
        this.stp = stp;
        this.delay = delay;
        this.fwDev = fwNic;
        this.brIpAddr = ipAddr;
        this.bridgeNetMask = netMask;
    }

    public void defLocalNetwork(final String brName, final boolean stp, final int delay, final String ipAddr, final String netMask) {
        this.networkType = NetworkType.LOCAL;
        this.bridgeName = brName;
        this.stp = stp;
        this.delay = delay;
        this.brIpAddr = ipAddr;
        this.bridgeNetMask = netMask;
    }

    public void adddhcpIpRange(final String start, final String end) {
        final IpRange ipr = new IpRange(start, end);
        this.ipranges.add(ipr);
    }

    public void adddhcpMapping(final String mac, final String host, final String ip) {
        final DhcpMapping map = new DhcpMapping(mac, host, ip);
        this.dhcpMaps.add(map);
    }

    @Override
    public String toString() {
        final StringBuilder netBuilder = new StringBuilder();
        netBuilder.append("<network>\n");
        netBuilder.append("<name>" + this.networkName + "</name>\n");
        if (this.uuid != null) {
            netBuilder.append("<uuid>" + this.uuid + "</uuid>\n");
        }
        if (this.bridgeName != null) {
            netBuilder.append("<bridge name='" + this.bridgeName + "'");
            if (this.stp) {
                netBuilder.append(" stp='on'");
            } else {
                netBuilder.append(" stp='off'");
            }
            if (this.delay != -1) {
                netBuilder.append(" delay='" + this.delay + "'");
            }
            netBuilder.append("/>\n");
        }
        if (this.domainName != null) {
            netBuilder.append("<domain name='" + this.domainName + "'/>\n");
        }
        if (this.networkType == NetworkType.BRIDGE) {
            netBuilder.append("<forward mode='route'");
            if (this.fwDev != null) {
                netBuilder.append(" dev='" + this.fwDev + "'");
            }
            netBuilder.append("/>\n");
        } else if (this.networkType == NetworkType.NAT) {
            netBuilder.append("<forward mode='nat'");
            if (this.fwDev != null) {
                netBuilder.append(" dev='" + this.fwDev + "'");
            }
            netBuilder.append("/>\n");
        }
        if (this.brIpAddr != null || this.bridgeNetMask != null || !this.ipranges.isEmpty() || !this.dhcpMaps.isEmpty()) {
            netBuilder.append("<ip");
            if (this.brIpAddr != null) {
                netBuilder.append(" address='" + this.brIpAddr + "'");
            }
            if (this.bridgeNetMask != null) {
                netBuilder.append(" netmask='" + this.bridgeNetMask + "'");
            }
            netBuilder.append(">\n");

            if (!this.ipranges.isEmpty() || !this.dhcpMaps.isEmpty()) {
                netBuilder.append("<dhcp>\n");
                for (final IpRange ip : this.ipranges) {
                    netBuilder.append("<range start='" + ip.start + "'" + " end='" + ip.end + "'/>\n");
                }
                for (final DhcpMapping map : this.dhcpMaps) {
                    netBuilder.append("<host mac='" + map.mac + "' name='" + map.name + "' ip='" + map.ip + "'/>\n");
                }
                netBuilder.append("</dhcp>\n");
            }
            netBuilder.append("</ip>\n");
        }
        netBuilder.append("</network>\n");
        return netBuilder.toString();
    }

    enum NetworkType {
        BRIDGE, NAT, LOCAL
    }

    public static class DhcpMapping {
        String mac;
        String name;
        String ip;

        public DhcpMapping(final String mac, final String name, final String ip) {
            this.mac = mac;
            this.name = name;
            this.ip = ip;
        }
    }

    public static class IpRange {
        String start;
        String end;

        public IpRange(final String start, final String end) {
            this.start = start;
            this.end = end;
        }
    }
}
