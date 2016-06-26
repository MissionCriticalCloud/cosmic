package org.apache.cloudstack.framework.security.keystore;

import com.cloud.utils.db.Encrypt;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "keystore")
public class KeystoreVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "certificate", length = 65535)
    private String certificate;

    @Encrypt
    @Column(name = "key", length = 65535)
    private String key;

    @Column(name = "domain_suffix")
    private String domainSuffix;

    @Column(name = "seq")
    private Integer index;

    public KeystoreVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(final String certificate) {
        this.certificate = certificate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getDomainSuffix() {
        return domainSuffix;
    }

    public void setDomainSuffix(final String domainSuffix) {
        this.domainSuffix = domainSuffix;
    }

    public Integer getIndex() {
        return this.index;
    }

    public void setIndex(final Integer index) {
        this.index = index;
    }
}
