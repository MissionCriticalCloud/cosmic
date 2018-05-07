package com.cloud.hypervisor.kvm.resource;

import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.legacymodel.to.NicTO;

import javax.naming.ConfigurationException;
import java.util.Map;

import org.libvirt.LibvirtException;

public interface VifDriver {

    void configure(Map<String, Object> params) throws ConfigurationException;

    LibvirtVmDef.InterfaceDef plug(NicTO nic, String guestOsType, String nicAdapter)
            throws InternalErrorException, LibvirtException;

    void unplug(LibvirtVmDef.InterfaceDef iface);
}
