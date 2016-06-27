package com.cloud.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "ssh_keypairs")
public class SSHKeyPairVO implements SSHKeyPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id = null;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "keypair_name")
    private String name;

    @Column(name = "fingerprint")
    private String fingerprint;

    @Column(name = "public_key", length = 5120)
    private String publicKey;

    @Transient
    private String privateKey;

    @Override
    public long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(final long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFingerprint() {
        return fingerprint;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(final String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(final String publicKey) {
        this.publicKey = publicKey;
    }

    public void setFingerprint(final String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Class<?> getEntityType() {
        return SSHKeyPair.class;
    }
}
