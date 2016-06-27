package com.cloud.vm;

import org.apache.cloudstack.api.ResourceDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_vm_details")
public class UserVmDetailVO implements ResourceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "vm_id")
    private long resourceId;

    @Column(name = "name")
    private String name;

    @Column(name = "value", length = 5120)
    private String value;

    @Column(name = "display")
    private boolean display = true;

    public UserVmDetailVO() {
    }

    public UserVmDetailVO(final long vmId, final String name, final String value, final boolean display) {
        this.resourceId = vmId;
        this.name = name;
        this.value = value;
        this.display = display;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getResourceId() {
        return resourceId;
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
}
