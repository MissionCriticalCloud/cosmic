package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.NicIpAlias;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "nic_ip_alias")
public class NicIpAliasVO implements NicIpAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "nic_Id")
    long nicId;
    @Column(name = "domain_id", updatable = false)
    long domainId;
    @Column(name = "ip4_address")
    String ip4Address;
    @Column(name = "ip6_address")
    String ip6Address;
    @Column(name = "netmask")
    String netmask;
    @Column(name = "network_id")
    long networkId;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "uuid")
    String uuid = UUID.randomUUID().toString();
    @Column(name = "vmId")
    Long vmId;
    @Column(name = "alias_count")
    Long aliasCount;
    @Column(name = "gateway")
    String gateway;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    NicIpAlias.State state;
    @Column(name = "start_ip_of_subnet")
    String startIpOfSubnet;
    @Column(name = "account_id", updatable = false)
    private Long accountId;

    public NicIpAliasVO(final Long nicId, final String ipaddr, final Long vmId, final Long accountId, final Long domainId, final Long networkId, final String gateway, final
    String netmask) {
        this.nicId = nicId;
        this.vmId = vmId;
        ip4Address = ipaddr;
        this.accountId = accountId;
        this.domainId = domainId;
        this.networkId = networkId;
        this.netmask = netmask;
        this.gateway = gateway;
        state = NicIpAlias.State.active;
        final String cidr = NetUtils.getCidrFromGatewayAndNetmask(gateway, netmask);
        final String[] cidrPair = cidr.split("\\/");
        final String cidrAddress = cidrPair[0];
        final long cidrSize = Long.parseLong(cidrPair[1]);
        startIpOfSubnet = NetUtils.getIpRangeStartIpFromCidr(cidrAddress, cidrSize);
    }

    protected NicIpAliasVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getNicId() {
        return nicId;
    }

    public void setNicId(final long nicId) {
        this.nicId = nicId;
    }

    @Override
    public String getIp4Address() {
        return ip4Address;
    }

    public void setIp4Address(final String ip4Address) {
        this.ip4Address = ip4Address;
    }

    @Override
    public String getIp6Address() {
        return ip6Address;
    }

    public void setIp6Address(final String ip6Address) {
        this.ip6Address = ip6Address;
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(final long networkId) {
        this.networkId = networkId;
    }

    @Override
    public long getVmId() {
        return vmId;
    }

    public void setVmId(final Long vmId) {
        this.vmId = vmId;
    }

    @Override
    public Long getAliasCount() {
        return aliasCount;
    }

    public void setAliasCount(final long count) {
        aliasCount = count;
    }

    @Override
    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public NicIpAlias.State getState() {
        return state;
    }

    public void setState(final NicIpAlias.State state) {
        this.state = state;
    }

    public String getStartIpOfSubnet() {
        return startIpOfSubnet;
    }

    @Override
    public Class<?> getEntityType() {
        return NicIpAlias.class;
    }
}
