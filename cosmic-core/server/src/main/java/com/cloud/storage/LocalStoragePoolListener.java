package com.cloud.storage;

import com.cloud.agent.Listener;
import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.StartupCommand;
import com.cloud.agent.api.StartupStorageCommand;
import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.exception.ConnectionException;
import com.cloud.host.Host;
import com.cloud.host.Status;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.utils.db.DB;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;

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
    public boolean processDisconnect(final long agentId, final Status state) {
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
