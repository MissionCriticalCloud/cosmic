//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = RebootCommand.class)
public final class LibvirtRebootCommandWrapper extends CommandWrapper<RebootCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtRebootCommandWrapper.class);

    @Override
    public Answer execute(final RebootCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

        try {
            final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName());
            final String result = libvirtComputingResource.rebootVm(conn, command.getVmName());
            if (result == null) {
                Integer vncPort = null;
                try {
                    vncPort = libvirtComputingResource.getVncPort(conn, command.getVmName());
                } catch (final LibvirtException e) {
                    s_logger.trace("Ignoring libvirt error.", e);
                }
                libvirtComputingResource.getRuleLogsForVms();
                return new RebootAnswer(command, null, vncPort);
            } else {
                return new RebootAnswer(command, result, false);
            }
        } catch (final LibvirtException e) {
            return new RebootAnswer(command, e.getMessage(), false);
        }
    }
}
