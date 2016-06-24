package com.cloud.hypervisor.kvm.resource.wrapper;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.FenceAnswer;
import com.cloud.agent.api.FenceCommand;
import com.cloud.hypervisor.kvm.resource.KvmHaBase.NfsStoragePool;
import com.cloud.hypervisor.kvm.resource.KvmHaChecker;
import com.cloud.hypervisor.kvm.resource.KvmHaMonitor;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ResourceWrapper(handles = FenceCommand.class)
public final class LibvirtFenceCommandWrapper extends CommandWrapper<FenceCommand, Answer, LibvirtComputingResource> {

    private static final Logger s_logger = LoggerFactory.getLogger(LibvirtFenceCommandWrapper.class);

    @Override
    public Answer execute(final FenceCommand command, final LibvirtComputingResource libvirtComputingResource) {
        final ExecutorService executors = Executors.newSingleThreadExecutor();
        final KvmHaMonitor monitor = libvirtComputingResource.getMonitor();

        final List<NfsStoragePool> pools = monitor.getStoragePools();

        /**
         * We can only safely fence off hosts when we use NFS On NFS primary storage pools hosts continuesly write a
         * heartbeat. Disable Fencing Off for hosts without NFS
         */
        if (pools.size() == 0) {
            final String logline = "No NFS storage pools found. No way to safely fence " + command.getVmName() + " on host "
                    + command.getHostGuid();
            s_logger.warn(logline);
            return new FenceAnswer(command, false, logline);
        }

        final KvmHaChecker ha = new KvmHaChecker(pools, command.getHostIp());

        final Future<Boolean> future = executors.submit(ha);
        try {
            final Boolean result = future.get();
            if (result) {
                return new FenceAnswer(command, false, "Heart is still beating...");
            } else {
                return new FenceAnswer(command);
            }
        } catch (final InterruptedException e) {
            s_logger.warn("Unable to fence", e);
            return new FenceAnswer(command, false, e.getMessage());
        } catch (final ExecutionException e) {
            s_logger.warn("Unable to fence", e);
            return new FenceAnswer(command, false, e.getMessage());
        }
    }
}
