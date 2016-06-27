package com.cloud.storage.listener;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupRoutingCommand;
import com.cloud.exception.ConnectionException;
import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.OCFS2Manager;
import com.cloud.storage.ScopeType;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageManagerImpl;
import com.cloud.storage.StoragePoolStatus;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoragePoolMonitor implements Listener {

    private static final Logger s_logger = LoggerFactory.getLogger(StoragePoolMonitor.class);
    private final StorageManagerImpl _storageManager;
    private final PrimaryDataStoreDao _poolDao;
    @Inject
    OCFS2Manager _ocfs2Mgr;

    public StoragePoolMonitor(final StorageManagerImpl mgr, final PrimaryDataStoreDao poolDao) {
        _storageManager = mgr;
        _poolDao = poolDao;
    }

    @Override
    public synchronized boolean processAnswers(final long agentId, final long seq, final Answer[] resp) {
        return true;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] req) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        return null;
    }

    @Override
    public void processConnect(final Host host, final StartupCommand cmd, final boolean forRebalance) throws ConnectionException {
        if (cmd instanceof StartupRoutingCommand) {
            final StartupRoutingCommand scCmd = (StartupRoutingCommand) cmd;
            if (scCmd.getHypervisorType() == HypervisorType.XenServer || scCmd.getHypervisorType() == HypervisorType.KVM || scCmd.getHypervisorType() == HypervisorType.Ovm3) {
                final List<StoragePoolVO> pools = _poolDao.listBy(host.getDataCenterId(), host.getPodId(), host.getClusterId(), ScopeType.CLUSTER);
                final List<StoragePoolVO> zoneStoragePoolsByTags = _poolDao.findZoneWideStoragePoolsByTags(host.getDataCenterId(), null);
                final List<StoragePoolVO> zoneStoragePoolsByHypervisor = _poolDao.findZoneWideStoragePoolsByHypervisor(host.getDataCenterId(), scCmd.getHypervisorType());
                zoneStoragePoolsByTags.retainAll(zoneStoragePoolsByHypervisor);
                pools.addAll(zoneStoragePoolsByTags);
                final List<StoragePoolVO> zoneStoragePoolsByAnyHypervisor = _poolDao.findZoneWideStoragePoolsByHypervisor(host.getDataCenterId(), HypervisorType.Any);
                pools.addAll(zoneStoragePoolsByAnyHypervisor);

                for (final StoragePoolVO pool : pools) {
                    if (pool.getStatus() != StoragePoolStatus.Up) {
                        continue;
                    }
                    if (!pool.isShared()) {
                        continue;
                    }

                    if (pool.getPoolType() == StoragePoolType.OCFS2 && !_ocfs2Mgr.prepareNodes(pool.getClusterId())) {
                        throw new ConnectionException(true, "Unable to prepare OCFS2 nodes for pool " + pool.getId());
                    }

                    final Long hostId = host.getId();
                    s_logger.debug("Host " + hostId + " connected, sending down storage pool information ...");
                    try {
                        _storageManager.connectHostToSharedPool(hostId, pool.getId());
                        _storageManager.createCapacityEntry(pool.getId());
                    } catch (final Exception e) {
                        s_logger.warn("Unable to connect host " + hostId + " to pool " + pool + " due to " + e.toString(), e);
                    }
                }
            }
        }
    }

    @Override
    public synchronized boolean processDisconnect(final long agentId, final Status state) {
        return true;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public int getTimeout() {
        return -1;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        return true;
    }
}
