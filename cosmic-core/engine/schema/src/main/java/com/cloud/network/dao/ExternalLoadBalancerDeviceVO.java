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
 * ExternalLoadBalancerDeviceVO contains information on external load balancer devices (VPX,MPX,SDX) added into a deployment
 */

@Entity
@Table(name = "external_load_balancer_devices")
public class ExternalLoadBalancerDeviceVO implements InternalIdentity, Identity {

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

    @Column(name = "device_state")
    @Enumerated(value = EnumType.STRING)
    private LBDeviceState state;

    @Column(name = "allocation_state")
    @Enumerated(value = EnumType.STRING)
    private LBDeviceAllocationState allocationState;

    @Column(name = "is_managed")
    private boolean isManagedDevice;

    @Column(name = "is_dedicated")
    private boolean isDedicatedDevice;

    @Column(name = "is_gslb_provider")
    private boolean gslbProvider;

    @Column(name = "is_exclusive_gslb_provider")
    private boolean exclusiveGslbProvider;

    @Column(name = "gslb_site_publicip")
    private String gslbSitePublicIP;

    @Column(name = "gslb_site_privateip")
    private String gslbSitePrivateIP;

    @Column(name = "parent_host_id")
    private long parentHostId;

    @Column(name = "capacity")
    private long capacity;

    public ExternalLoadBalancerDeviceVO(final long hostId, final long physicalNetworkId, final String providerName, final String deviceName, final long capacity, final boolean
            dedicated, final boolean managed,
                                        final long parentHostId) {
        this(hostId, physicalNetworkId, providerName, deviceName, capacity, dedicated, false);
        this.isManagedDevice = managed;
        this.parentHostId = parentHostId;
    }

    public ExternalLoadBalancerDeviceVO(final long hostId, final long physicalNetworkId, final String providerName, final String deviceName, final long capacity, final boolean
            dedicated,
                                        final boolean gslbProvider) {
        this.physicalNetworkId = physicalNetworkId;
        this.providerName = providerName;
        this.deviceName = deviceName;
        this.hostId = hostId;
        this.state = LBDeviceState.Disabled;
        this.allocationState = LBDeviceAllocationState.Free;
        this.capacity = capacity;
        this.isDedicatedDevice = dedicated;
        this.isManagedDevice = false;
        this.state = LBDeviceState.Enabled;
        this.uuid = UUID.randomUUID().toString();
        this.gslbProvider = gslbProvider;
        this.gslbSitePublicIP = null;
        this.gslbSitePrivateIP = null;
    }

    public ExternalLoadBalancerDeviceVO() {
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

    public long getParentHostId() {
        return parentHostId;
    }

    public void setParentHostId(final long parentHostId) {
        this.parentHostId = parentHostId;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(final long capacity) {
        this.capacity = capacity;
    }

    public LBDeviceState getState() {
        return state;
    }

    public void setState(final LBDeviceState state) {
        this.state = state;
    }

    public LBDeviceAllocationState getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final LBDeviceAllocationState allocationState) {
        this.allocationState = allocationState;
    }

    public boolean getIsManagedDevice() {
        return isManagedDevice;
    }

    public void setIsManagedDevice(final boolean managed) {
        this.isManagedDevice = managed;
    }

    public boolean getIsDedicatedDevice() {
        return isDedicatedDevice;
    }

    public void setIsDedicatedDevice(final boolean isDedicated) {
        isDedicatedDevice = isDedicated;
    }

    public boolean getGslbProvider() {
        return gslbProvider;
    }

    public void setGslbProvider(final boolean gslbProvider) {
        this.gslbProvider = gslbProvider;
    }

    public boolean getExclusiveGslbProvider() {
        return exclusiveGslbProvider;
    }

    public void setExclusiveGslbProvider(final boolean exclusiveGslbProvider) {
        this.exclusiveGslbProvider = exclusiveGslbProvider;
    }

    public String getGslbSitePublicIP() {
        return gslbSitePublicIP;
    }

    public void setGslbSitePublicIP(final String gslbSitePublicIP) {
        this.gslbSitePublicIP = gslbSitePublicIP;
    }

    public String getGslbSitePrivateIP() {
        return gslbSitePrivateIP;
    }

    public void setGslbSitePrivateIP(final String gslbSitePrivateIP) {
        this.gslbSitePrivateIP = gslbSitePrivateIP;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    //keeping it enum for future possible states Maintenance, Shutdown
    public enum LBDeviceState {
        Enabled, Disabled
    }

    public enum LBDeviceAllocationState {
        Free,      // In this state no networks are using this device for load balancing
        Shared,    // In this state one or more networks will be using this device for load balancing
        Dedicated, // In this state this device is dedicated for a single network
        Provider   // This state is set only for device that can dynamically provision LB appliances
    }
}
