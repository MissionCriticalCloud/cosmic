//

//

package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.HashMap;
import java.util.List;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.GetVmStatsAnswer;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.VmStatsEntry;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

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
      final HashMap<String, VmStatsEntry> vmStatsNameMap = new HashMap<String, VmStatsEntry>();
      for (final String vmName : vmNames) {

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = libvirtComputingResource.getLibvirtUtilitiesHelper();

        final Connect conn = libvirtUtilitiesHelper.getConnectionByVmName(vmName);
        try {
          final VmStatsEntry statEntry = libvirtComputingResource.getVmStat(conn, vmName);
          if (statEntry == null) {
            continue;
          }

          vmStatsNameMap.put(vmName, statEntry);
        } catch (LibvirtException e) {
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