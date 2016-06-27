//

//

package com.cloud.network;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "external_nicira_nvp_devices")
public class NiciraNvpDeviceVO implements InternalIdentity {

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

    public NiciraNvpDeviceVO() {
        uuid = UUID.randomUUID().toString();
    }

    public NiciraNvpDeviceVO(final long hostId, final long physicalNetworkId, final String providerName, final String deviceName) {
        super();
        this.hostId = hostId;
        this.physicalNetworkId = physicalNetworkId;
        this.providerName = providerName;
        this.deviceName = deviceName;
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public long getHostId() {
        return hostId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
