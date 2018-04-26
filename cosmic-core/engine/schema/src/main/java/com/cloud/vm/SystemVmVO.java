package com.cloud.vm;

import com.cloud.hypervisor.Hypervisor;

import javax.persistence.Column;

public abstract class SystemVmVO extends VMInstanceVO {

    @Column(name = "public_ip_address", nullable = false)
    protected String publicIpAddress;

    @Column(name = "public_mac_address", nullable = false)
    protected String publicMacAddress;

    @Column(name = "public_netmask", nullable = false)
    protected String publicNetmask;

    public SystemVmVO(final long id, final long serviceOfferingId, final String name, final long templateId,
                      final Hypervisor.HypervisorType hypervisorType, final long domainId, final long accountId, final long userId, final boolean haEnabled) {
        super(id, serviceOfferingId, name, name, Type.SecondaryStorageVm, templateId, hypervisorType, domainId, accountId, userId, haEnabled);
    }

    public SystemVmVO() {

    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getPublicNetmask() {
        return publicNetmask;
    }

    public void setPublicNetmask(final String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public String getPublicMacAddress() {
        return publicMacAddress;
    }

    public void setPublicMacAddress(final String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }
}
