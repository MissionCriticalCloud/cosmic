package com.cloud.vm;

import com.cloud.legacymodel.storage.SecondaryStorageVmRole;
import com.cloud.model.enumeration.ComplianceStatus;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.OptimiseFor;
import com.cloud.model.enumeration.VirtualMachineType;

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

import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * SecondaryStorageVmVO domain object
 */

@Entity
@Table(name = "secondary_storage_vm")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue(value = "SecondaryStorageVm")
public class SecondaryStorageVmVO extends VMInstanceVO implements SystemVm {
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
    private SecondaryStorageVmRole role;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update", updatable = true, nullable = true)
    private Date lastUpdateTime;

    public SecondaryStorageVmVO(final long id, final long serviceOfferingId, final String name, final long templateId, final HypervisorType hypervisorType, final long guestOSId,
                                final long dataCenterId, final long domainId, final long accountId, final long userId, final SecondaryStorageVmRole role, final boolean haEnabled,
                                final OptimiseFor optimiseFor, final String manufacturerString, final String cpuFlags, final Boolean macLearning, final Boolean requiresRestart,
                                final MaintenancePolicy maintenancePolicy) {
        super(id, serviceOfferingId, name, name, VirtualMachineType.SecondaryStorageVm, templateId, hypervisorType, guestOSId, domainId, accountId, userId, haEnabled);
        this.role = role;
        this.dataCenterId = dataCenterId;
        this.optimiseFor = optimiseFor;
        this.manufacturerString = manufacturerString;
        this.cpuFlags = cpuFlags;
        this.macLearning = macLearning;
        this.requiresRestart = requiresRestart;
        this.maintenancePolicy = maintenancePolicy;
        this.complianceStatus = ComplianceStatus.Compliant;
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

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public SecondaryStorageVmRole getRole() {
        return this.role;
    }

    public void setRole(final SecondaryStorageVmRole role) {
        this.role = role;
    }
}
