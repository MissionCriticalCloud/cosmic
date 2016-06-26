package com.cloud.network.dao;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * ExternalFirewallDeviceVO contains information of a external firewall device (Juniper SRX) added into a deployment
 */

@Entity
@Table(name = "external_firewall_devices")
public class ExternalFirewallDeviceVO implements InternalIdentity, Identity {
    @Column(name = "device_state")
    @Enumerated(value = EnumType.STRING)
    FirewallDeviceState deviceState;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "host_id")
    private long hostId;
    @Column(name = "physical_network_id")
    private long physicalNetworkId;
    @Column(name = "provider_name")
    private String providerName;
    @Column(name = "device_name")
    private String deviceName;
    @Column(name = "is_dedicated")
    private boolean isDedicatedDevice;

    @Column(name = "capacity")
    private long capacity;

    @Column(name = "allocation_state")
    @Enumerated(value = EnumType.STRING)
    private FirewallDeviceAllocationState allocationState;

    public ExternalFirewallDeviceVO(final long hostId, final long physicalNetworkId, final String providerName, final String deviceName, final long capacity, final boolean
            dedicated) {
        this.physicalNetworkId = physicalNetworkId;
        this.providerName = providerName;
        this.deviceName = deviceName;
        this.hostId = hostId;
        this.deviceState = FirewallDeviceState.Disabled;
        this.allocationState = FirewallDeviceAllocationState.Free;
        this.capacity = capacity;
        this.isDedicatedDevice = dedicated;
        this.deviceState = FirewallDeviceState.Enabled;
        this.uuid = UUID.randomUUID().toString();
    }

    public ExternalFirewallDeviceVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public long getHostId() {
        return hostId;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(final long capacity) {
        this.capacity = capacity;
    }

    public FirewallDeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(final FirewallDeviceState state) {
        this.deviceState = state;
    }

    public FirewallDeviceAllocationState getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final FirewallDeviceAllocationState allocationState) {
        this.allocationState = allocationState;
    }

    public boolean getIsDedicatedDevice() {
        return isDedicatedDevice;
    }

    public void setIsDedicatedDevice(final boolean isDedicated) {
        isDedicatedDevice = isDedicated;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    //keeping it enum for future possible states Maintenance, Shutdown
    public enum FirewallDeviceState {
        Enabled, Disabled
    }

    public enum FirewallDeviceAllocationState {
        Free, Allocated
    }
}
