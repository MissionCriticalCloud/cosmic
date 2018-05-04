package com.cloud.network.dao;

import com.cloud.legacymodel.network.Ip;
import com.cloud.network.IpAddress;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

/**
 * A bean representing a public IP Address
 */
@Entity
@Table(name = ("user_ip_address"))
public class IPAddressVO implements IpAddress {
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "account_id")
    private Long allocatedToAccountId = null;
    @Column(name = "domain_id")
    private Long allocatedInDomainId = null;
    @Column(name = "public_ip_address")
    @Enumerated(value = EnumType.STRING)
    private Ip address = null;
    @Column(name = "data_center_id", updatable = false)
    private long dataCenterId;
    @Column(name = "source_nat")
    private boolean sourceNat;
    @Column(name = "allocated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date allocatedTime;
    @Column(name = "vlan_db_id")
    private long vlanId;
    @Column(name = "one_to_one_nat")
    private boolean oneToOneNat;
    @Column(name = "vm_id")
    private Long associatedWithVmId;
    @Column(name = "state")
    private State state;
    @Column(name = "mac_address")
    private long macAddress;
    @Column(name = "source_network_id")
    private Long sourceNetworkId;
    @Column(name = "network_id")
    private Long associatedWithNetworkId;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "physical_network_id")
    private Long physicalNetworkId;
    @Column(name = "ip_acl_id")
    private Long ipACLId;
    @Column(name = "is_system")
    private boolean system;
    @Column(name = "account_id")
    @Transient
    private Long accountId = null;
    @Transient
    @Column(name = "domain_id")
    private Long domainId = null;
    @Column(name = "vpc_id")
    private Long vpcId;
    @Column(name = "dnat_vmip")
    private String vmIp;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    protected IPAddressVO() {
        uuid = UUID.randomUUID().toString();
        ipACLId = 2L; // Default Allow ACL
    }

    public IPAddressVO(final Ip address, final long dataCenterId, final long macAddress, final long vlanDbId, final boolean sourceNat) {
        this.address = address;
        this.dataCenterId = dataCenterId;
        vlanId = vlanDbId;
        this.sourceNat = sourceNat;
        allocatedInDomainId = null;
        allocatedToAccountId = null;
        allocatedTime = null;
        state = State.Free;
        this.macAddress = macAddress;
        uuid = UUID.randomUUID().toString();
        ipACLId = 2L; // Default Allow ACL
    }

    public IPAddressVO(final Ip address, final long dataCenterId, final Long networkId, final Long vpcId, final long physicalNetworkId, final long sourceNetworkId, final long
            vlanDbId) {
        this.address = address;
        this.dataCenterId = dataCenterId;
        associatedWithNetworkId = networkId;
        this.vpcId = vpcId;
        this.physicalNetworkId = physicalNetworkId;
        this.sourceNetworkId = sourceNetworkId;
        vlanId = vlanDbId;
        uuid = UUID.randomUUID().toString();
        ipACLId = 2L; // Default Allow ACL
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setAddress(final Ip address) {
        this.address = address;
    }

    public void setMacAddress(final long macAddress) {
        this.macAddress = macAddress;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public long getMacAddress() {
        return macAddress;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final long dcId) {
        dataCenterId = dcId;
    }

    @Override
    public Ip getAddress() {
        return address;
    }

    @Override
    public Date getAllocatedTime() {
        return allocatedTime;
    }

    @Override
    public boolean isSourceNat() {
        return sourceNat;
    }

    public void setSourceNat(final boolean sourceNat) {
        this.sourceNat = sourceNat;
    }

    @Override
    public long getVlanId() {
        return vlanId;
    }

    public void setVlanId(final long vlanDbId) {
        vlanId = vlanDbId;
    }

    @Override
    public boolean isOneToOneNat() {
        return oneToOneNat;
    }

    public void setOneToOneNat(final boolean oneToOneNat) {
        this.oneToOneNat = oneToOneNat;
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
    public boolean readyToUse() {
        return state == State.Allocated;
    }

    @Override
    public Long getAssociatedWithNetworkId() {
        return associatedWithNetworkId;
    }

    public void setAssociatedWithNetworkId(final Long networkId) {
        associatedWithNetworkId = networkId;
    }

    @Override
    public Long getAssociatedWithVmId() {
        return associatedWithVmId;
    }

    public void setAssociatedWithVmId(final Long associatedWithVmId) {
        this.associatedWithVmId = associatedWithVmId;
    }

    @Override
    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    @Override
    public Long getAllocatedToAccountId() {
        return allocatedToAccountId;
    }

    @Override
    public Long getAllocatedInDomainId() {
        return allocatedInDomainId;
    }

    public void setAllocatedInDomainId(final Long domainId) {
        allocatedInDomainId = domainId;
    }

    @Override
    public boolean getSystem() {
        return system;
    }

    public void setSystem(final boolean isSystem) {
        system = isSystem;
    }

    @Override
    public Long getVpcId() {
        return vpcId;
    }

    public void setVpcId(final Long vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public String getVmIp() {
        return vmIp;
    }

    @Override
    public Long getNetworkId() {
        return sourceNetworkId;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    public void setVmIp(final String vmIp) {
        this.vmIp = vmIp;
    }

    public void setAllocatedToAccountId(final Long accountId) {
        allocatedToAccountId = accountId;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public void setAllocatedTime(final Date allocated) {
        allocatedTime = allocated;
    }

    @Override
    public long getDomainId() {
        return allocatedInDomainId == null ? -1 : allocatedInDomainId;
    }

    @Override
    public long getAccountId() {
        return allocatedToAccountId == null ? -1 : allocatedToAccountId;
    }

    @Override
    public String toString() {
        return new StringBuilder("Ip[").append(address).append("-").append(dataCenterId).append("]").toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public Long getSourceNetworkId() {
        return sourceNetworkId;
    }

    public void setSourceNetworkId(final Long sourceNetworkId) {
        this.sourceNetworkId = sourceNetworkId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return IpAddress.class;
    }

    @Override
    public Long getIpACLId() {
        return ipACLId;
    }

    public void setIpACLId(final Long ipACLId) {
        this.ipACLId = ipACLId;
    }
}
