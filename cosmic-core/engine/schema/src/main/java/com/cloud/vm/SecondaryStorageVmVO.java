package com.cloud.vm;

import com.cloud.hypervisor.Hypervisor.HypervisorType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * SecondaryStorageVmVO domain object
 */

@Entity
@Table(name = "secondary_storage_vm")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue(value = "SecondaryStorageVm")
public class SecondaryStorageVmVO extends VMInstanceVO implements SecondaryStorageVm {

    @Column(name = "public_ip_address", nullable = false)
    private String publicIpAddress;

    @Column(name = "public_mac_address", nullable = false)
    private String publicMacAddress;

    @Column(name = "public_netmask", nullable = false)
    private String publicNetmask;

    @Column(name = "guid", nullable = false)
    private String guid;

    @Column(name = "nfs_share", nullable = false)
    private String nfsShare;

    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update", updatable = true, nullable = true)
    private Date lastUpdateTime;

    public SecondaryStorageVmVO(final long id, final long serviceOfferingId, final String name, final long templateId, final HypervisorType hypervisorType, final long guestOSId,
                                final long dataCenterId,
                                final long domainId, final long accountId, final long userId, final Role role, final boolean haEnabled) {
        super(id, serviceOfferingId, name, name, Type.SecondaryStorageVm, templateId, hypervisorType, guestOSId, domainId, accountId, userId, haEnabled);
        this.role = role;
        this.dataCenterId = dataCenterId;
    }

    protected SecondaryStorageVmVO() {
        super();
    }

    @Override
    public String getPublicIpAddress() {
        return this.publicIpAddress;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    @Override
    public String getPublicNetmask() {
        return this.publicNetmask;
    }

    public void setPublicNetmask(final String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    @Override
    public String getPublicMacAddress() {
        return this.publicMacAddress;
    }

    public void setPublicMacAddress(final String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    @Override
    public Date getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(final Date time) {
        this.lastUpdateTime = time;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public String getNfsShare() {
        return nfsShare;
    }

    public void setNfsShare(final String nfsShare) {
        this.nfsShare = nfsShare;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }
}
