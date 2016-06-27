package com.cloud.network;

import com.cloud.utils.db.GenericDao;

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
@Table(name = ("user_ipv6_address"))
public class UserIpv6AddressVO implements UserIpv6Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "ip_address")
    @Enumerated(value = EnumType.STRING)
    private String address = null;
    @Column(name = "data_center_id", updatable = false)
    private long dataCenterId;
    @Column(name = "vlan_id")
    private long vlanId;
    @Column(name = "state")
    private State state;
    @Column(name = "mac_address")
    private String macAddress;
    @Column(name = "source_network_id")
    private Long sourceNetworkId;
    @Column(name = "network_id")
    private Long networkId;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "physical_network_id")
    private Long physicalNetworkId;
    @Column(name = "account_id")
    private Long accountId = null;
    @Column(name = "domain_id")
    private Long domainId = null;

    protected UserIpv6AddressVO() {
        uuid = UUID.randomUUID().toString();
    }

    public UserIpv6AddressVO(final String address, final long dataCenterId, final String macAddress, final long vlanDbId) {
        this.address = address;
        this.dataCenterId = dataCenterId;
        vlanId = vlanDbId;
        state = State.Free;
        setMacAddress(macAddress);
        uuid = UUID.randomUUID().toString();
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public void setVlanId(final long vlanId) {
        this.vlanId = vlanId;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public long getVlanId() {
        return vlanId;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public Long getNetworkId() {
        return networkId;
    }

    @Override
    public Long getSourceNetworkId() {
        return sourceNetworkId;
    }

    @Override
    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    @Override
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public void setSourceNetworkId(final Long sourceNetworkId) {
        this.sourceNetworkId = sourceNetworkId;
    }

    public void setNetworkId(final Long networkId) {
        this.networkId = networkId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    @Override
    public Class<?> getEntityType() {
        return UserIpv6Address.class;
    }
}
