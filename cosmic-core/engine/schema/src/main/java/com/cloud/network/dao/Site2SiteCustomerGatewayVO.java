package com.cloud.network.dao;

import com.cloud.network.Site2SiteCustomerGateway;
import com.cloud.utils.db.Encrypt;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = ("s2s_customer_gateway"))
public class Site2SiteCustomerGatewayVO implements Site2SiteCustomerGateway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "gateway_ip")
    private String gatewayIp;

    @Column(name = "guest_cidr_list")
    private String guestCidrList;

    @Encrypt
    @Column(name = "ipsec_psk")
    private String ipsecPsk;

    @Column(name = "ike_policy")
    private String ikePolicy;

    @Column(name = "esp_policy")
    private String espPolicy;

    @Column(name = "ike_lifetime")
    private long ikeLifetime;

    @Column(name = "esp_lifetime")
    private long espLifetime;

    @Column(name = "dpd")
    private boolean dpd;

    @Column(name = "force_encap")
    private boolean encap;

    @Column(name = "domain_id")
    private Long domainId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    public Site2SiteCustomerGatewayVO() {
    }

    public Site2SiteCustomerGatewayVO(final String name, final long accountId, final long domainId, final String gatewayIp, final String guestCidrList, final String ipsecPsk,
                                      final String ikePolicy,
                                      final String espPolicy, final long ikeLifetime, final long espLifetime, final boolean dpd, final boolean encap) {
        this.name = name;
        this.gatewayIp = gatewayIp;
        this.guestCidrList = guestCidrList;
        this.ipsecPsk = ipsecPsk;
        this.ikePolicy = ikePolicy;
        this.espPolicy = espPolicy;
        this.ikeLifetime = ikeLifetime;
        this.espLifetime = espLifetime;
        this.dpd = dpd;
        this.encap = encap;
        uuid = UUID.randomUUID().toString();
        this.accountId = accountId;
        this.domainId = domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getGatewayIp() {
        return gatewayIp;
    }

    public void setGatewayIp(final String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    @Override
    public String getGuestCidrList() {
        return guestCidrList;
    }

    public void setGuestCidrList(final String guestCidrList) {
        this.guestCidrList = guestCidrList;
    }

    @Override
    public String getIpsecPsk() {
        return ipsecPsk;
    }

    public void setIpsecPsk(final String ipsecPsk) {
        this.ipsecPsk = ipsecPsk;
    }

    @Override
    public String getIkePolicy() {
        return ikePolicy;
    }

    public void setIkePolicy(final String ikePolicy) {
        this.ikePolicy = ikePolicy;
    }

    @Override
    public String getEspPolicy() {
        return espPolicy;
    }

    @Override
    public Long getIkeLifetime() {
        return ikeLifetime;
    }

    public void setIkeLifetime(final long ikeLifetime) {
        this.ikeLifetime = ikeLifetime;
    }

    @Override
    public Long getEspLifetime() {
        return espLifetime;
    }

    public void setEspLifetime(final long espLifetime) {
        this.espLifetime = espLifetime;
    }

    @Override
    public Boolean getDpd() {
        return dpd;
    }

    public void setDpd(final boolean dpd) {
        this.dpd = dpd;
    }

    @Override
    public Boolean getEncap() {
        return encap;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setEncap(final boolean encap) {
        this.encap = encap;
    }

    public void setEspPolicy(final String espPolicy) {
        this.espPolicy = espPolicy;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public Class<?> getEntityType() {
        return Site2SiteCustomerGateway.class;
    }
}
