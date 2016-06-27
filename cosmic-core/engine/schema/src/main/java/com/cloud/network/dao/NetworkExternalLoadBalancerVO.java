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
 * NetworkExternalLoadBalancerVO contains mapping of a network and the external load balancer device id assigned to the network
 */

@Entity
@Table(name = "network_external_lb_device_map")
public class NetworkExternalLoadBalancerVO implements InternalIdentity {

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
    @Column(name = "external_load_balancer_device_id")
    private long externalLBDeviceId;

    public NetworkExternalLoadBalancerVO(final long networkId, final long externalLBDeviceID) {
        this.networkId = networkId;
        this.externalLBDeviceId = externalLBDeviceID;
        this.uuid = UUID.randomUUID().toString();
    }

    public NetworkExternalLoadBalancerVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public long getNetworkId() {
        return networkId;
    }

    public long getExternalLBDeviceId() {
        return externalLBDeviceId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
