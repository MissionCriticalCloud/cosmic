package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.common.virtualnetwork.VirtualRoutingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.CheckSshAnswer;
import com.cloud.legacymodel.communication.command.CheckSshCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = CheckSshCommand.class)
public final class LibvirtCheckSshCommandWrapper
        extends LibvirtCommandWrapper<CheckSshCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtCheckSshCommandWrapper.class);

    @Override
    public Answer execute(final CheckSshCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final String vmName = command.getName();
        final String privateIp = command.getIp();
        final int cmdPort = command.getPort();

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Ping command port, " + privateIp + ":" + cmdPort);
        }

        final VirtualRoutingResource virtRouterResource = libvirtComputingResource.getVirtRouterResource();
        if (!virtRouterResource.connect(privateIp, cmdPort)) {
            return new CheckSshAnswer(command, "Can not ping System vm " + vmName + " because of a connection failure");
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Ping command port succeeded for vm " + vmName);
        }

        return new CheckSshAnswer(command);
    }
}
