package com.cloud.vm;

import com.cloud.network.Networks.AddressFormat;
import com.cloud.network.Networks.Mode;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "nics")
public class NicVO implements Nic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "instance_id")
    Long instanceId;
    @Column(name = "ip4_address")
    String iPv4Address;
    @Column(name = "ip6_address")
    String iPv6Address;
    @Column(name = "netmask")
    String iPv4Netmask;
    @Column(name = "isolation_uri")
    URI isolationUri;
    @Column(name = "ip_type")
    AddressFormat addressFormat;
    @Column(name = "broadcast_uri")
    URI broadcastUri;
    @Column(name = "gateway")
    String iPv4Gateway;
    @Column(name = "mac_address")
    String macAddress;
    @Column(name = "mode")
    @Enumerated(value = EnumType.STRING)
    Mode mode;
    @Column(name = "network_id")
    long networkId;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    State state;
    @Column(name = "reserver_name")
    String reserver;
    @Column(name = "reservation_id")
    String reservationId;
    @Column(name = "device_id")
    int deviceId;
    @Column(name = "update_time")
    Date updateTime;
    @Column(name = "default_nic")
    boolean defaultNic;
    @Column(name = "ip6_gateway")
    String iPv6Gateway;
    @Column(name = "ip6_cidr")
    String iPv6Cidr;
    @Column(name = "strategy")
    @Enumerated(value = EnumType.STRING)
    ReservationStrategy reservationStrategy;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "vm_type")
    VirtualMachine.Type vmType;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "uuid")
    String uuid = UUID.randomUUID().toString();
    @Column(name = "secondary_ip")
    boolean secondaryIp;

    protected NicVO() {
    }

    public NicVO(final String reserver, final Long instanceId, final long configurationId, final VirtualMachine.Type vmType) {
        this.reserver = reserver;
        this.instanceId = instanceId;
        this.networkId = configurationId;
        this.state = State.Allocated;
        this.vmType = vmType;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(final String id) {
        this.reservationId = id;
    }

    @Override
    public String getReserver() {
        return reserver;
    }

    public void setReserver(final String reserver) {
        this.reserver = reserver;
    }

    @Override
    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public ReservationStrategy getReservationStrategy() {
        return reservationStrategy;
    }

    @Override
    public boolean isDefaultNic() {
        return defaultNic;
    }

    @Override
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(final long networkId) {
        this.networkId = networkId;
    }

    @Override
    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(final long instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public URI getIsolationUri() {
        return isolationUri;
    }

    public void setIsolationUri(final URI isolationUri) {
        this.isolationUri = isolationUri;
    }

    @Override
    public URI getBroadcastUri() {
        return broadcastUri;
    }

    public void setBroadcastUri(final URI broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    @Override
    public VirtualMachine.Type getVmType() {
        return vmType;
    }

    @Override
    public AddressFormat getAddressFormat() {
        return addressFormat;
    }

    public void setAddressFormat(final AddressFormat format) {
        this.addressFormat = format;
    }

    @Override
    public boolean getSecondaryIp() {
        return secondaryIp;
    }

    @Override
    public String getIPv4Address() {
        return iPv4Address;
    }

    public void setIPv4Address(final String address) {
        iPv4Address = address;
    }

    @Override
    public String getIPv4Netmask() {
        return iPv4Netmask;
    }

    @Override
    public String getIPv4Gateway() {
        return iPv4Gateway;
    }

    public void setIPv4Gateway(final String gateway) {
        this.iPv4Gateway = gateway;
    }

    @Override
    public String getIPv6Gateway() {
        return iPv6Gateway;
    }

    public void setIPv6Gateway(final String ip6Gateway) {
        this.iPv6Gateway = ip6Gateway;
    }

    @Override
    public String getIPv6Cidr() {
        return iPv6Cidr;
    }

    @Override
    public String getIPv6Address() {
        return iPv6Address;
    }

    public void setIPv6Address(final String ip6Address) {
        this.iPv6Address = ip6Address;
    }

    public void setIPv6Cidr(final String ip6Cidr) {
        this.iPv6Cidr = ip6Cidr;
    }

    public void setIPv4Netmask(final String netmask) {
        this.iPv4Netmask = netmask;
    }

    public void setSecondaryIp(final boolean secondaryIp) {
        this.secondaryIp = secondaryIp;
    }

    public void setVmType(final VirtualMachine.Type vmType) {
        this.vmType = vmType;
    }

    public void setMode(final Mode mode) {
        this.mode = mode;
    }

    public void setDefaultNic(final boolean defaultNic) {
        this.defaultNic = defaultNic;
    }

    public void setReservationStrategy(final ReservationStrategy strategy) {
        this.reservationStrategy = strategy;
    }

    public void setUpdateTime(final Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return new StringBuilder("Nic[").append(id)
                                        .append("-")
                                        .append(instanceId)
                                        .append("-")
                                        .append(reservationId)
                                        .append("-")
                                        .append(iPv4Address)
                                        .append("]")
                                        .toString();
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
