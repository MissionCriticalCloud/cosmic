package com.cloud.agent.resource.kvm.async;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;

import java.util.concurrent.Callable;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class MigrateKvmAsync implements Callable<Domain> {

    private final LibvirtComputingResource libvirtComputingResource;

    private Domain dm = null;
    private Connect dconn = null;
    private String dxml = "";
    private String vmName = "";
    private String destIp = "";

    public MigrateKvmAsync(final LibvirtComputingResource libvirtComputingResource, final Domain dm, final Connect dconn,
                           final String dxml, final String vmName, final String destIp) {
        this.libvirtComputingResource = libvirtComputingResource;

        this.dm = dm;
        this.dconn = dconn;
        this.dxml = dxml;
        this.vmName = vmName;
        this.destIp = destIp;
    }

    @Override
    public Domain call() throws LibvirtException {
        // set compression flag for migration if libvirt version supports it
        if (this.dconn.getLibVirVersion() < 1003000) {
            return this.dm.migrate(this.dconn, 1 << 0, this.dxml, this.vmName, "tcp:" + this.destIp, this.libvirtComputingResource.getMigrateSpeed());
        } else {
            return this.dm.migrate(this.dconn, 1 << 0 | 1 << 11, this.dxml, this.vmName, "tcp:" + this.destIp,
                    this.libvirtComputingResource.getMigrateSpeed());
        }
    }
}
