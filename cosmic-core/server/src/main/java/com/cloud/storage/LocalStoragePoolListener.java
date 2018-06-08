package com.cloud.storage;

import com.cloud.agent.Listener;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.agentcontrol.AgentControlCommand;
import com.cloud.legacymodel.communication.command.startup.StartupCommand;
import com.cloud.legacymodel.communication.command.startup.StartupLocalstorageCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.legacymodel.storage.StoragePoolInfo;
import com.cloud.model.enumeration.StoragePoolStatus;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.utils.db.DB;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalStoragePoolListener implements Listener {
    private final static Logger s_logger = LoggerFactory.getLogger(LocalStoragePoolListener.class);
    @Inject
    PrimaryDataStoreDao _storagePoolDao;
    @Inject
    StoragePoolHostDao _storagePoolHostDao;
    @Inject
    CapacityDao _capacityDao;
    @Inject
    StorageManager _storageMgr;
    @Inject
    DataCenterDao _dcDao;

    @Override
    public boolean processAnswers(final long agentId, final long seq, final Answer[] answers) {
        return false;
    }

    @Override
    public boolean processCommands(final long agentId, final long seq, final Command[] commands) {
        return false;
    }

    @Override
    public AgentControlAnswer processControlCommand(final long agentId, final AgentControlCommand cmd) {
        return null;
    }

    @Override
    @DB
    public void processConnect(final Host host, final StartupCommand[] startupCommands, final boolean forRebalance) throws ConnectionException {
        final List<StoragePoolVO> registeredStoragePoolsForHost = this._storagePoolDao.listHostScopedPoolsByStorageHost(host.getName());
        final List<StoragePoolVO> liveStoragePools = new ArrayList<>();

        for (final StartupCommand startupCommand : startupCommands) {
            if (startupCommand instanceof StartupLocalstorageCommand) {
                final StartupLocalstorageCommand ssCmd = (StartupLocalstorageCommand) startupCommand;

                final StoragePoolInfo pInfo = ssCmd.getPoolInfo();
                if (pInfo == null) {
                    return;
                }

                s_logger.info("Found storage pool in StartupCommand creating it now: " + pInfo.getUuid());
                liveStoragePools.add((StoragePoolVO) this._storageMgr.createLocalStorage(host, pInfo));
            }
        }

        registeredStoragePoolsForHost.removeAll(liveStoragePools);

        registeredStoragePoolsForHost.forEach(storagePoolVO -> {
            // Disable all storage pools not live right now!
            s_logger.info("Disabling storage pool because it is not in the StartupCommand: " + storagePoolVO.getName());
            storagePoolVO.setStatus(StoragePoolStatus.Disabled);
            this._storagePoolDao.persist(storagePoolVO);
        });
    }

    @Override
    public boolean processDisconnect(final long agentId, final HostStatus state) {
        return false;
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public boolean processTimeout(final long agentId, final long seq) {
        return false;
    }
}
