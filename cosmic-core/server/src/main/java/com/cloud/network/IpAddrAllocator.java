package com.cloud.network;

import com.cloud.utils.component.Adapter;

public interface IpAddrAllocator extends Adapter {
    public IpAddr getPublicIpAddress(String macAddr, long dcId, long podId);

    public IpAddr getPrivateIpAddress(String macAddr, long dcId, long podId);

    public boolean releasePublicIpAddress(String ip, long dcId, long podId);

    public boolean releasePrivateIpAddress(String ip, long dcId, long podId);

    public boolean externalIpAddressAllocatorEnabled();

    public class IpAddr {
        public String ipaddr;
        public String netMask;
        public String gateway;

        public IpAddr(final String ipaddr, final String netMask, final String gateway) {
            this.ipaddr = ipaddr;
            this.netMask = netMask;
            this.gateway = gateway;
        }

        public IpAddr() {
            this.ipaddr = null;
            this.netMask = null;
            this.gateway = null;
        }
    }

    public class NetworkInfo {
        public String _ipAddr;
        public String _netMask;
        public String _gateWay;
        public Long _vlanDbId;
        public String _vlanid;

        public NetworkInfo(final String ip, final String netMask, final String gateway, final Long vlanDbId, final String vlanId) {
            _ipAddr = ip;
            _netMask = netMask;
            _gateWay = gateway;
            _vlanDbId = vlanDbId;
            _vlanid = vlanId;
        }
    }
}
