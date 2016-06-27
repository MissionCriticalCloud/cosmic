package org.apache.cloudstack.resourcedetail;

import org.apache.cloudstack.api.ResourceDetail;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "remote_access_vpn_details")
public class RemoteAccessVpnDetailVO implements ResourceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "remote_access_vpn_id")
    private long resourceId;

    @Column(name = "name")
    private String name;

    @Column(name = "value", length = 1024)
    private String value;

    @Column(name = "display")
    private boolean display = true;

    public RemoteAccessVpnDetailVO() {
    }

    public RemoteAccessVpnDetailVO(final long id, final String name, final String value, final boolean display) {
        this.resourceId = id;
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
