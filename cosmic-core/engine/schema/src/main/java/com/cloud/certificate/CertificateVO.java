package com.cloud.certificate;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "certificate")
public class CertificateVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id = null;

    @Column(name = "certificate", length = 65535)
    private String certificate;

    @Column(name = "updated")
    private String updated;

    public CertificateVO() {
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(final String certificate) {
        this.certificate = certificate;
    }

    public String getUpdated() {
        return this.updated;
    }

    public void setUpdated(final String updated) {
        this.updated = updated;
    }
}
