package com.cloud.service;

import org.apache.cloudstack.api.ResourceDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "service_offering_details")
public class ServiceOfferingDetailsVO implements ResourceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "service_offering_id")
    private long resourceId;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    @Column(name = "display")
    private boolean display = true;

    protected ServiceOfferingDetailsVO() {
    }

    public ServiceOfferingDetailsVO(final long serviceOfferingId, final String name, final String value, final boolean display) {
        this.resourceId = serviceOfferingId;
        this.name = name;
        this.value = value;
        this.display = display;
    }

    @Override
    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(final long serviceOfferingId) {
        this.resourceId = serviceOfferingId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    @Override
    public long getId() {
        return id;
    }
}
