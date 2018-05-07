package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.GetVmStatsAnswer;
import com.cloud.legacymodel.communication.command.GetVmStatsCommand;
import com.cloud.legacymodel.vm.VmStatsEntry;

import java.util.HashMap;
import java.util.List;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetVmStatsCommand.class)
public final class LibvirtGetVmStatsCommandWrapper
        extends CommandWrapper<GetVmStatsCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtGetVmStatsCommandWrapper.class);

    @Override
    public Answer execute(final GetVmStatsCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final List<String> vmNames = command.getVmNames();
        try {
            final HashMap<String, VmStatsEntry> vmStatsNameMap = new HashMap<>();
            for (final String vmName : vmNames) {

                final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

                final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
                try {
                    final VmStatsEntry statEntry = libvirtComputingResource.getVmStat(conn, vmName);
                    if (statEntry == null) {
                        continue;
                    }

                    vmStatsNameMap.put(vmName, statEntry);
                } catch (final LibvirtException e) {
                    s_logger.warn("Can't get vm stats: " + e.toString() + ", continue");
                }
            }
            return new GetVmStatsAnswer(command, vmStatsNameMap);
        } catch (final LibvirtException e) {
            s_logger.debug("Can't get vm stats: " + e.toString());
            return new GetVmStatsAnswer(command, null);
        }
    }
}
