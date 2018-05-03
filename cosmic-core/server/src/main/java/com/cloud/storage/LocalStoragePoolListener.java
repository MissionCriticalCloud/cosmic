package com.cloud.storage;

import com.cloud.agent.Listener;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.AgentControlCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.utils.db.DB;

import javax.inject.Inject;

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
    public void processConnect(final Host host, final StartupCommand cmd, final boolean forRebalance) throws ConnectionException {
        if (!(cmd instanceof StartupStorageCommand)) {
            return;
        }

        final StartupStorageCommand ssCmd = (StartupStorageCommand) cmd;

        if (ssCmd.getResourceType() != Storage.StorageResourceType.STORAGE_POOL) {
            return;
        }

        final StoragePoolInfo pInfo = ssCmd.getPoolInfo();
        if (pInfo == null) {
            return;
        }

        this._storageMgr.createLocalStorage(host, pInfo);
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
