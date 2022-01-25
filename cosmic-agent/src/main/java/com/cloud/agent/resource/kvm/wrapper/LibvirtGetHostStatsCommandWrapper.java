package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.GetHostStatsAnswer;
import com.cloud.legacymodel.communication.command.GetHostStatsCommand;
import com.cloud.legacymodel.dc.HostStatsEntry;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.utils.linux.CpuStat;
import com.cloud.utils.linux.MemStat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetHostStatsCommand.class)
public final class LibvirtGetHostStatsCommandWrapper
        extends LibvirtCommandWrapper<GetHostStatsCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtGetHostStatsCommandWrapper.class);

    @Override
    public Answer execute(final GetHostStatsCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final CpuStat cpuStat = libvirtComputingResource.getCpuStat();
        final MemStat memStat = libvirtComputingResource.getMemStat();

        final double cpuUtil = cpuStat.getCpuUsedPercent();

        final Pair<Double, Double> nicStats = libvirtComputingResource.getNicStats(
                libvirtComputingResource.getPublicBridgeName());

        final HostStatsEntry hostStats = new HostStatsEntry(command.getHostId(), cpuUtil, nicStats.first() / 1024,
                nicStats.second() / 1024, "host", memStat.getTotal() /1024, memStat.getAvailable() /1024, 0, 0);
        return new GetHostStatsAnswer(command, hostStats);
    }
}
