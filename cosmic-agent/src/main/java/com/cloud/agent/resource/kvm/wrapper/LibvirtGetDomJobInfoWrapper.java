package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.MigrationProgressAnswer;
import com.cloud.legacymodel.communication.command.MigrationProgressCommand;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainJobInfo;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = MigrationProgressCommand.class)
public final class LibvirtGetDomJobInfoWrapper extends LibvirtCommandWrapper<MigrationProgressCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtGetDomJobInfoWrapper.class);

    @Override
    public Answer execute(final MigrationProgressCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final String vmName = command.getVmName();
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();
        final Connect conn;
        DomainJobInfo domainJobInfo;
        Domain vm;

        try {
            conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
            vm = libvirtComputingResource.getDomain(conn, vmName);
            domainJobInfo = vm.getJobInfo();
        } catch (LibvirtException e) {
            final String msg = " Getting domain job info failed due to " + e.toString();
            s_logger.warn(msg, e);
            return new MigrationProgressAnswer(command, false, msg);
        }

        return new MigrationProgressAnswer(command, true, null,
                domainJobInfo.getTimeElapsed(), domainJobInfo.getTimeRemaining(),
                domainJobInfo.getDataTotal(), domainJobInfo.getDataProcessed(), domainJobInfo.getDataRemaining(),
                domainJobInfo.getMemTotal(), domainJobInfo.getMemProcessed(), domainJobInfo.getMemRemaining(),
                domainJobInfo.getFileTotal(), domainJobInfo.getFileProcessed(), domainJobInfo.getFileRemaining()
        );
    }
}
