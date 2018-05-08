package com.cloud.agent.resource.kvm.wrapper;

import com.cloud.agent.resource.kvm.KvmHaBase.NfsStoragePool;
import com.cloud.agent.resource.kvm.KvmHaChecker;
import com.cloud.agent.resource.kvm.KvmHaMonitor;
import com.cloud.agent.resource.kvm.LibvirtComputingResource;
import com.cloud.common.request.CommandWrapper;
import com.cloud.common.request.ResourceWrapper;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.CheckOnHostCommand;
import com.cloud.legacymodel.to.HostTO;
import com.cloud.legacymodel.to.NetworkTO;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
