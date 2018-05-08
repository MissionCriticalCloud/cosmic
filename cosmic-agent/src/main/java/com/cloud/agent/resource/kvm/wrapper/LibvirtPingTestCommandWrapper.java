package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.PingTestCommand;
import com.cloud.utils.script.Script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = PingTestCommand.class)
public final class LibvirtPingTestCommandWrapper
        extends CommandWrapper<PingTestCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtPingTestCommandWrapper.class);

    @Override
    public Answer execute(final PingTestCommand command, final LibvirtComputingResource libvirtComputingResource) {
        String result = null;
        final String computingHostIp = command.getComputingHostIp(); // TODO, split the command into 2 types

        if (computingHostIp != null) {
            result = doPingTest(libvirtComputingResource, computingHostIp);
        } else if (command.getRouterIp() != null && command.getPrivateIp() != null) {
            result = doPingTest(libvirtComputingResource, command.getRouterIp(), command.getPrivateIp());
        } else {
            return new Answer(command, false, "routerip and private ip is null");
        }

        if (result != null) {
            return new Answer(command, false, result);
        }
        return new Answer(command);
    }

    protected String doPingTest(final LibvirtComputingResource libvirtComputingResource, final String computingHostIp) {
        final Script command = new Script(libvirtComputingResource.getPingTestPath(), 10000, s_logger);
        command.add("-h", computingHostIp);
        return command.execute();
    }

    protected String doPingTest(final LibvirtComputingResource libvirtComputingResource, final String domRIp,
                                final String vmIp) {
        final Script command = new Script(libvirtComputingResource.getPingTestPath(), 10000, s_logger);
        command.add("-i", domRIp);
        command.add("-p", vmIp);
        return command.execute();
    }
}
