//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetHostStatsAnswer;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.HostStatsEntry;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;
import com.cloud.utils.Pair;

import org.apache.cloudstack.utils.linux.CpuStat;
import org.apache.cloudstack.utils.linux.MemStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetHostStatsCommand.class)
public final class LibvirtGetHostStatsCommandWrapper
    extends CommandWrapper<GetHostStatsCommand, Answer, LibvirtComputingResource> {

  private static final Logger s_logger = LoggerFactory.getLogger(LibvirtGetHostStatsCommandWrapper.class);

  @Override
  public Answer execute(final GetHostStatsCommand command, final LibvirtComputingResource libvirtComputingResource) {
    CpuStat cpuStat = libvirtComputingResource.getCpuStat();
    MemStat memStat = libvirtComputingResource.getMemStat();

    final double cpuUtil = cpuStat.getCpuUsedPercent();
    memStat.refresh();
    double totMem = memStat.getTotal();
    double freeMem = memStat.getAvailable();

    final Pair<Double, Double> nicStats = libvirtComputingResource.getNicStats(
        libvirtComputingResource.getPublicBridgeName());

    final HostStatsEntry hostStats = new HostStatsEntry(command.getHostId(), cpuUtil, nicStats.first() / 1024,
        nicStats.second() / 1024, "host", totMem, freeMem, 0, 0);
    return new GetHostStatsAnswer(command, hostStats);
  }
}