package com.cloud.hypervisor.kvm.resource;

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
        networkType = NetworkType.NAT;
        bridgeName = brName;
        this.stp = stp;
        this.delay = delay;
        fwDev = fwNic;
        brIpAddr = ipAddr;
        bridgeNetMask = netMask;
    }

    public void defBrNetwork(final String brName, final boolean stp, final int delay, final String fwNic, final String ipAddr, final String netMask) {
        networkType = NetworkType.BRIDGE;
        bridgeName = brName;
        this.stp = stp;
        this.delay = delay;
        fwDev = fwNic;
        brIpAddr = ipAddr;
        bridgeNetMask = netMask;
    }

    public void defLocalNetwork(final String brName, final boolean stp, final int delay, final String ipAddr, final String netMask) {
        networkType = NetworkType.LOCAL;
        bridgeName = brName;
        this.stp = stp;
        this.delay = delay;
        brIpAddr = ipAddr;
        bridgeNetMask = netMask;
    }

    public void adddhcpIpRange(final String start, final String end) {
        final IpRange ipr = new IpRange(start, end);
        ipranges.add(ipr);
    }

    public void adddhcpMapping(final String mac, final String host, final String ip) {
        final DhcpMapping map = new DhcpMapping(mac, host, ip);
        dhcpMaps.add(map);
    }

    @Override
    public String toString() {
        final StringBuilder netBuilder = new StringBuilder();
        netBuilder.append("<network>\n");
        netBuilder.append("<name>" + networkName + "</name>\n");
        if (uuid != null) {
            netBuilder.append("<uuid>" + uuid + "</uuid>\n");
        }
        if (bridgeName != null) {
            netBuilder.append("<bridge name='" + bridgeName + "'");
            if (stp) {
                netBuilder.append(" stp='on'");
            } else {
                netBuilder.append(" stp='off'");
            }
            if (delay != -1) {
                netBuilder.append(" delay='" + delay + "'");
            }
            netBuilder.append("/>\n");
        }
        if (domainName != null) {
            netBuilder.append("<domain name='" + domainName + "'/>\n");
        }
        if (networkType == NetworkType.BRIDGE) {
            netBuilder.append("<forward mode='route'");
            if (fwDev != null) {
                netBuilder.append(" dev='" + fwDev + "'");
            }
            netBuilder.append("/>\n");
        } else if (networkType == NetworkType.NAT) {
            netBuilder.append("<forward mode='nat'");
            if (fwDev != null) {
                netBuilder.append(" dev='" + fwDev + "'");
            }
            netBuilder.append("/>\n");
        }
        if (brIpAddr != null || bridgeNetMask != null || !ipranges.isEmpty() || !dhcpMaps.isEmpty()) {
            netBuilder.append("<ip");
            if (brIpAddr != null) {
                netBuilder.append(" address='" + brIpAddr + "'");
            }
            if (bridgeNetMask != null) {
                netBuilder.append(" netmask='" + bridgeNetMask + "'");
            }
            netBuilder.append(">\n");

            if (!ipranges.isEmpty() || !dhcpMaps.isEmpty()) {
                netBuilder.append("<dhcp>\n");
                for (final IpRange ip : ipranges) {
                    netBuilder.append("<range start='" + ip.start + "'" + " end='" + ip.end + "'/>\n");
                }
                for (final DhcpMapping map : dhcpMaps) {
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
