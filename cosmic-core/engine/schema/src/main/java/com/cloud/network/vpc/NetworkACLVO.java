package com.cloud.network.vpc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "network_acl")
public class NetworkACLVO implements NetworkACL {

    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Column(name = "vpc_id")
    Long vpcId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;

    public NetworkACLVO() {
    }

    protected NetworkACLVO(final String name, final String description, final long vpcId) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.vpcId = vpcId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public Long getVpcId() {
        return vpcId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
