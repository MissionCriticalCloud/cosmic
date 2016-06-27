package com.cloud.hypervisor.kvm.resource;

import com.cloud.agent.api.to.NicTO;
import com.cloud.exception.InternalErrorException;

import javax.naming.ConfigurationException;
import java.util.Map;

import org.libvirt.LibvirtException;

public interface VifDriver {

    public void configure(Map<String, Object> params) throws ConfigurationException;

    public LibvirtVmDef.InterfaceDef plug(NicTO nic, String guestOsType, String nicAdapter)
            throws InternalErrorException, LibvirtException;

    public void unplug(LibvirtVmDef.InterfaceDef iface);
}
