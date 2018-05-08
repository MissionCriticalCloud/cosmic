package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.agent.resource.kvm.LibvirtVmDef.InterfaceDef;
import com.cloud.agent.resource.kvm.VifDriver;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.PlugNicAnswer;
import com.cloud.legacymodel.communication.command.PlugNicCommand;
import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.legacymodel.to.NicTO;

import java.util.List;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = PlugNicCommand.class)
public final class LibvirtPlugNicCommandWrapper
        extends CommandWrapper<PlugNicCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtPlugNicCommandWrapper.class);

    @Override
    public Answer execute(final PlugNicCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final NicTO nic = command.getNic();
        final String vmName = command.getVmName();
        Domain vm = null;
        try {
            final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
            final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
            vm = libvirtComputingResource.getDomain(conn, vmName);

            final List<InterfaceDef> pluggedNics = libvirtComputingResource.getInterfaces(conn, vmName);
            Integer nicnum = 0;
            for (final InterfaceDef pluggedNic : pluggedNics) {
                if (pluggedNic.getMacAddress().equalsIgnoreCase(nic.getMac())) {
                    s_logger.debug("found existing nic for mac " + pluggedNic.getMacAddress() + " at index " + nicnum);
                    return new PlugNicAnswer(command, true, "success");
                }
                nicnum++;
            }
            final VifDriver vifDriver = libvirtComputingResource.getVifDriver(nic.getType());
            final InterfaceDef interfaceDef = vifDriver.plug(nic, "Default - VirtIO capable OS (64-bit)", "");
            vm.attachDevice(interfaceDef.toString());

            return new PlugNicAnswer(command, true, "success");
        } catch (final LibvirtException e) {
            final String msg = " Plug Nic failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new PlugNicAnswer(command, false, msg);
        } catch (final InternalErrorException e) {
            final String msg = " Plug Nic failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new PlugNicAnswer(command, false, msg);
        } finally {
            if (vm != null) {
                try {
                    vm.free();
                } catch (final LibvirtException l) {
                    s_logger.trace("Ignoring libvirt error.", l);
                }
            }
        }
    }
}
