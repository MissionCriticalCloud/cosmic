package com.cloud.hypervisor.kvm.resource.wrapper;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.agent.api.to.HostTO;
import com.cloud.agent.api.to.NetworkTO;
import com.cloud.hypervisor.kvm.resource.KvmHaBase.NfsStoragePool;
import com.cloud.hypervisor.kvm.resource.KvmHaChecker;
import com.cloud.hypervisor.kvm.resource.KvmHaMonitor;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

@ResourceWrapper(handles = CheckOnHostCommand.class)
public final class LibvirtCheckOnHostCommandWrapper
    extends CommandWrapper<CheckOnHostCommand, Answer, LibvirtComputingResource> {

  @Override
  public Answer execute(final CheckOnHostCommand command, final LibvirtComputingResource libvirtComputingResource) {
    final ExecutorService executors = Executors.newSingleThreadExecutor();
    final KvmHaMonitor monitor = libvirtComputingResource.getMonitor();

    final List<NfsStoragePool> pools = monitor.getStoragePools();
    final HostTO host = command.getHost();
    final NetworkTO privateNetwork = host.getPrivateNetwork();
    final KvmHaChecker ha = new KvmHaChecker(pools, privateNetwork.getIp());

    final Future<Boolean> future = executors.submit(ha);
    try {
      final Boolean result = future.get();
      if (result) {
        return new Answer(command, false, "Heart is still beating...");
      } else {
        return new Answer(command);
      }
    } catch (final InterruptedException e) {
      return new Answer(command, false, "can't get status of host:");
    } catch (final ExecutionException e) {
      return new Answer(command, false, "can't get status of host:");
    }
  }
}