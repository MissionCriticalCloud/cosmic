package com.cloud.network.vpc;

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
@Table(name = "vpc_offering_service_map")
public class VpcOfferingServiceMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "vpc_offering_id")
    long vpcOfferingId;

    @Column(name = "service")
    String service;

    @Column(name = "provider")
    String provider;

    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;

    public VpcOfferingServiceMapVO() {
    }

    public VpcOfferingServiceMapVO(final long vpcOfferingId, final Service service, final Provider provider) {
        this.vpcOfferingId = vpcOfferingId;
        this.service = service.getName();
        if (provider != null) {
            this.provider = provider.getName();
        }
    }

    @Override
    public long getId() {
        return id;
    }

    public long getVpcOfferingId() {
        return vpcOfferingId;
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
        final StringBuilder buf = new StringBuilder("[VPC Offering Service[");
        return buf.append(vpcOfferingId).append("-").append(service).append("-").append(provider).append("]").toString();
    }
}
