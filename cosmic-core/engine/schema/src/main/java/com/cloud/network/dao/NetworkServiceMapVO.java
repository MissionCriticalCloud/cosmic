package com.cloud.network.dao;

import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "ntwk_service_map")
public class NetworkServiceMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "network_id")
    long networkId;

    @Column(name = "service")
    String service;

    @Column(name = "provider")
    String provider;

    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;

    public NetworkServiceMapVO() {
    }

    public NetworkServiceMapVO(final long networkId, final Service service, final Provider provider) {
        this.networkId = networkId;
        this.service = service.getName();
        this.provider = provider.getName();
    }

    @Override
    public long getId() {
        return id;
    }

    public long getNetworkId() {
        return networkId;
    }

    public String getService() {
        return service;
    }

    public String getProvider() {
        return provider;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("[Network Service[");
        return buf.append(networkId).append("-").append(service).append("-").append(provider).append("]").toString();
    }
}
