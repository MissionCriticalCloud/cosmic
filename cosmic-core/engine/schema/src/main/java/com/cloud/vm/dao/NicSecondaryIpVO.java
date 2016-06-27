package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.NicSecondaryIp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "nic_secondary_ips")
public class NicSecondaryIpVO implements NicSecondaryIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "nicId")
    long nicId;
    @Column(name = "domain_id", updatable = false)
    long domainId;
    @Column(name = "ip4_address")
    String ip4Address;
    @Column(name = "ip6_address")
    String ip6Address;
    @Column(name = "network_id")
    long networkId;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "uuid")
    String uuid = UUID.randomUUID().toString();
    @Column(name = "vmId")
    long vmId;
    @Column(name = "account_id", updatable = false)
    private long accountId;

    public NicSecondaryIpVO(final long nicId, final String ipaddr, final long vmId, final long accountId, final long domainId, final long networkId) {
        this.nicId = nicId;
        this.vmId = vmId;
        ip4Address = ipaddr;
        this.accountId = accountId;
        this.domainId = domainId;
        this.networkId = networkId;
    }

    protected NicSecondaryIpVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getNicId() {
        return nicId;
    }

    @Override
    public String getIp4Address() {
        return ip4Address;
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    @Override
    public long getVmId() {
        return vmId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public String getIp6Address() {
        return ip6Address;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return NicSecondaryIp.class;
    }
}
