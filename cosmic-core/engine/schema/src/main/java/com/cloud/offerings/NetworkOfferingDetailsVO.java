package com.cloud.offerings;

import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Detail;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "network_offering_details")
public class NetworkOfferingDetailsVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "network_offering_id")
    private long offeringId;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "name")
    private NetworkOffering.Detail name;

    @Column(name = "value", length = 1024)
    private String value;

    public NetworkOfferingDetailsVO() {
    }

    public NetworkOfferingDetailsVO(final long offeringId, final Detail detailName, final String value) {
        this.offeringId = offeringId;
        this.name = detailName;
        this.value = value;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(final long offeringId) {
        this.offeringId = offeringId;
    }

    public NetworkOffering.Detail getName() {
        return name;
    }

    public void setName(final NetworkOffering.Detail name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
