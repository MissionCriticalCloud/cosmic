package com.cloud.network.addr;

import com.cloud.dc.VlanVO;
import com.cloud.network.IpAddress;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.NetUtils;

import java.util.Date;

/**
 */
public class PublicIp implements PublicIpAddress {
    IPAddressVO _addr;
    VlanVO _vlan;
    String macAddress;

    public PublicIp(final IPAddressVO addr, final VlanVO vlan, final long macAddress) {
        _addr = addr;
        _vlan = vlan;
        this.macAddress = NetUtils.long2Mac(macAddress);
    }

    public static PublicIp createFromAddrAndVlan(final IPAddressVO addr, final VlanVO vlan) {
        return new PublicIp(addr, vlan, NetUtils.createSequenceBasedMacAddress(addr.getMacAddress()));
    }

    @Override
    public String getVlanTag() {
        return _vlan.getVlanTag();
    }

    @Override
    public String getVlanGateway() {
        return _vlan.getVlanGateway();
    }

    @Override
    public String getVlanNetmask() {
        return _vlan.getVlanNetmask();
    }

    @Override
    public String getIpRange() {
        return _vlan.getIpRange();
    }

    @Override
    public VlanType getVlanType() {
        return _vlan.getVlanType();
    }

    @Override
    public String getIp6Gateway() {
        return _vlan.getIp6Gateway();
    }

    @Override
    public String getIp6Cidr() {
        return _vlan.getIp6Cidr();
    }

    @Override
    public String getIp6Range() {
        return _vlan.getIp6Range();
    }

    @Override
    public long getDataCenterId() {
        return _addr.getDataCenterId();
    }

    @Override
    public Ip getAddress() {
        return _addr.getAddress();
    }

    @Override
    public Date getAllocatedTime() {
        return _addr.getAllocatedTime();
    }

    @Override
    public boolean isSourceNat() {
        return _addr.isSourceNat();
    }

    @Override
    public long getVlanId() {
        return _vlan.getId();
    }

    @Override
    public boolean isOneToOneNat() {
        return _addr.isOneToOneNat();
    }

    @Override
    public State getState() {
        return _addr.getState();
    }

    @Override
    public void setState(final State state) {
        _addr.setState(state);
    }

    @Override
    public boolean readyToUse() {
        return _addr.getAllocatedTime() != null && _addr.getState() == State.Allocated;
    }

    @Override
    public Long getAssociatedWithNetworkId() {
        return _addr.getAssociatedWithNetworkId();
    }

    @Override
    public Long getAssociatedWithVmId() {
        return _addr.getAssociatedWithVmId();
    }

    @Override
    public Long getPhysicalNetworkId() {
        return _vlan.getPhysicalNetworkId();
    }

    @Override
    public Long getAllocatedToAccountId() {
        return _addr.getAllocatedToAccountId();
    }

    @Override
    public Long getAllocatedInDomainId() {
        return _addr.getAllocatedInDomainId();
    }

    @Override
    public boolean getSystem() {
        return _addr.getSystem();
    }

    @Override
    public Long getVpcId() {
        return _addr.getVpcId();
    }

    @Override
    public String getVmIp() {
        return _addr.getVmIp();
    }

    @Override
    public boolean isPortable() {
        return _addr.isPortable();
    }

    @Override
    public Long getNetworkId() {
        return _vlan.getNetworkId();
    }

    @Override
    public boolean isDisplay() {
        return _addr.isDisplay();
    }

    @Override
    public Date getRemoved() {
        return _addr.getRemoved();
    }

    @Override
    public Date getCreated() {
        return _addr.getCreated();
    }

    public void setPortable(final boolean portable) {
        _addr.setPortable(portable);
    }

    @Override
    public long getAccountId() {
        return _addr.getAccountId();
    }

    @Override
    public long getDomainId() {
        return _addr.getDomainId();
    }

    public IPAddressVO ip() {
        return _addr;
    }

    public VlanVO vlan() {
        return _vlan;
    }

    @Override
    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String getNetmask() {
        return _vlan.getVlanNetmask();
    }

    @Override
    public String getGateway() {
        return _vlan.getVlanGateway();
    }

    @Override
    public long getId() {
        return _addr.getId();
    }

    @Override
    public String getUuid() {
        return _addr.getUuid();
    }

    @Override
    public String toString() {
        return _addr.getAddress().toString();
    }

    public Long getIpMacAddress() {
        return _addr.getMacAddress();
    }

    @Override
    public Class<?> getEntityType() {
        return IpAddress.class;
    }
}
