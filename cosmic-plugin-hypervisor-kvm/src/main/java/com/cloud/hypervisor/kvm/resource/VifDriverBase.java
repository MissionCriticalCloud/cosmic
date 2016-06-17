package com.cloud.hypervisor.kvm.resource;

import java.util.Map;

import javax.naming.ConfigurationException;

import com.cloud.agent.api.to.NicTO;
import com.cloud.exception.InternalErrorException;

import org.libvirt.LibvirtException;

public abstract class VifDriverBase implements VifDriver {

  protected LibvirtComputingResource libvirtComputingResource;
  protected Map<String, String> pifs;
  protected Map<String, String> bridges;

  @SuppressWarnings("unchecked")
  @Override
  public void configure(Map<String, Object> params) throws ConfigurationException {
    libvirtComputingResource = (LibvirtComputingResource) params.get("libvirt.computing.resource");
    bridges = (Map<String, String>) params.get("libvirt.host.bridges");
    pifs = (Map<String, String>) params.get("libvirt.host.pifs");
  }

  @Override
  public abstract LibvirtVmDef.InterfaceDef plug(NicTO nic, String guestOsType, String nicAdapter)
      throws InternalErrorException, LibvirtException;

  @Override
  public abstract void unplug(LibvirtVmDef.InterfaceDef iface);

  protected LibvirtVmDef.InterfaceDef.NicModel getGuestNicModel(String platformEmulator, String nicAdapter) {
    // if nicAdapter is found in ENUM, use it. Otherwise, match guest OS type as before
    if (nicAdapter != null && !nicAdapter.isEmpty()) {
      for (final LibvirtVmDef.InterfaceDef.NicModel model : LibvirtVmDef.InterfaceDef.NicModel.values()) {
        if (model.toString().equalsIgnoreCase(nicAdapter)) {
          return model;
        }
      }
    }

    if (libvirtComputingResource.isGuestPvEnabled(platformEmulator)) {
      return LibvirtVmDef.InterfaceDef.NicModel.VIRTIO;
    } else {
      return LibvirtVmDef.InterfaceDef.NicModel.E1000;
    }
  }
}
