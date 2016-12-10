package com.cloud.network.dao;

import com.cloud.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "load_balancer_cert_map")
public class LoadBalancerCertMapVO implements InternalIdentity {

    @Column(name = "uuid")
    private String uuid;
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "load_balancer_id")
    private Long lbId;

    @Column(name = "certificate_id")
    private Long certId;

    @Column(name = "revoke")
    private boolean revoke = false;

    public LoadBalancerCertMapVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public LoadBalancerCertMapVO(final Long lbId, final Long certId, final boolean revoke) {

        this.lbId = lbId;
        this.certId = certId;
        this.revoke = revoke;
        this.uuid = UUID.randomUUID().toString();
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getLbId() {
        return lbId;
    }

    public void setLbId(final Long lbId) {
        this.lbId = lbId;
    }

    public Long getCertId() {
        return certId;
    }

    public void setCertId(final Long certId) {
        this.certId = certId;
    }

    public boolean isRevoke() {
        return revoke;
    }

    public void setRevoke(final boolean revoke) {
        this.revoke = revoke;
    }
}
