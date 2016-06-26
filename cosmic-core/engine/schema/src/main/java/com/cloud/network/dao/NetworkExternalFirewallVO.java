package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

/**
 * NetworkExternalFirewallVO contains information on the networks that are using external firewall
 */

@Entity
@Table(name = "network_external_firewall_device_map")
public class NetworkExternalFirewallVO implements InternalIdentity {
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "network_id")
    private long networkId;
    @Column(name = "external_firewall_device_id")
    private long externalFirewallDeviceId;

    public NetworkExternalFirewallVO(final long networkId, final long externalFirewallDeviceId) {
        this.networkId = networkId;
        this.externalFirewallDeviceId = externalFirewallDeviceId;
        this.uuid = UUID.randomUUID().toString();
    }

    public NetworkExternalFirewallVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public long getNetworkId() {
        return networkId;
    }

    public long getExternalFirewallDeviceId() {
        return externalFirewallDeviceId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
