package com.cloud.agent.resource.kvm;

import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.legacymodel.to.NicTO;
import com.cloud.model.enumeration.NicModel;

import javax.naming.ConfigurationException;
import java.util.Map;

import org.libvirt.LibvirtException;

public abstract class VifDriverBase implements VifDriver {

    protected LibvirtComputingResource libvirtComputingResource;
    protected Map<String, String> pifs;
    protected Map<String, String> bridges;

    @Override
    public void configure(final Map<String, Object> params) throws ConfigurationException {
        this.libvirtComputingResource = (LibvirtComputingResource) params.get("libvirt.computing.resource");
        this.bridges = (Map<String, String>) params.get("libvirt.host.bridges");
        this.pifs = (Map<String, String>) params.get("libvirt.host.pifs");
    }

    @Override
    public abstract LibvirtVmDef.InterfaceDef plug(NicTO nic, String guestOsType, String nicAdapter)
            throws InternalErrorException, LibvirtException;

    @Override
    public abstract void unplug(LibvirtVmDef.InterfaceDef iface);

    protected NicModel getGuestNicModel(final String platformEmulator, final String nicAdapter) {
        // if nicAdapter is found in ENUM, use it. Otherwise, match guest OS type as before
        if (nicAdapter != null && !nicAdapter.isEmpty()) {
            for (final NicModel model : NicModel.values()) {
                if (model.toString().equalsIgnoreCase(nicAdapter)) {
                    return model;
                }
            }
        }

        if (this.libvirtComputingResource.isGuestVirtIoCapable(platformEmulator)) {
            return NicModel.VIRTIO;
        } else {
            return NicModel.E1000;
        }
    }
}
