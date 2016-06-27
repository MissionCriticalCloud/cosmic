package com.cloud.offerings;

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
@Table(name = "ntwk_offering_service_map")
public class NetworkOfferingServiceMapVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "network_offering_id")
    long networkOfferingId;

    @Column(name = "service")
    String service;

    @Column(name = "provider")
    String provider;

    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;

    public NetworkOfferingServiceMapVO() {
    }

    public NetworkOfferingServiceMapVO(final long networkOfferingId, final Service service, final Provider provider) {
        this.networkOfferingId = networkOfferingId;
        this.service = service.getName();
        if (provider != null) {
            this.provider = provider.getName();
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public long getNetworkOfferingId() {
        return networkOfferingId;
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
        final StringBuilder buf = new StringBuilder("[Network Offering Service[");
        return buf.append(networkOfferingId).append("-").append(service).append("-").append(provider).append("]").toString();
    }
}
