//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.HashMap;
import java.util.List;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetVmDiskStatsAnswer;
import com.cloud.agent.api.GetVmDiskStatsCommand;
import com.cloud.agent.api.VmDiskStatsEntry;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = GetVmDiskStatsCommand.class)
public final class LibvirtGetVmDiskStatsCommandWrapper
    extends CommandWrapper<GetVmDiskStatsCommand, Answer, LibvirtComputingResource> {

  private static final Logger s_logger = LoggerFactory.getLogger(LibvirtGetVmDiskStatsCommandWrapper.class);

  @Override
  public Answer execute(final GetVmDiskStatsCommand command, final LibvirtComputingResource libvirtComputingResource) {
    final List<String> vmNames = command.getVmNames();
    final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

    try {
      final HashMap<String, List<VmDiskStatsEntry>> vmDiskStatsNameMap = new HashMap<String, List<VmDiskStatsEntry>>();
      final Connect conn = libvirtUtilitiesHelper.getConnection();
      for (final String vmName : vmNames) {
        try {
          final List<VmDiskStatsEntry> statEntry = libvirtComputingResource.getVmDiskStat(conn, vmName);
          if (statEntry == null) {
            continue;
          }

          vmDiskStatsNameMap.put(vmName, statEntry);
        } catch (LibvirtException e) {
          s_logger.warn("Can't get vm disk stats: " + e.toString() + ", continue");
        }
      }
      return new GetVmDiskStatsAnswer(command, "", command.getHostName(), vmDiskStatsNameMap);
    } catch (final LibvirtException e) {
      s_logger.debug("Can't get vm disk stats: " + e.toString());
      return new GetVmDiskStatsAnswer(command, null, null, null);
    }
  }
}
