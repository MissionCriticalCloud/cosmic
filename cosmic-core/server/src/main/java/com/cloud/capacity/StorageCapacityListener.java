package com.cloud.capacity;

import com.cloud.agent.Listener;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.legacymodel.communication.answer.AgentControlAnswer;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.agentcontrolcommand.AgentControlCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.legacymodel.communication.command.StartupStorageCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.ConnectionException;
import com.cloud.model.enumeration.StorageResourceType;
import com.cloud.storage.StorageManager;

import java.math.BigDecimal;

public class StorageCapacityListener implements Listener {

    CapacityDao _capacityDao;
    StorageManager _storageMgr;

    public StorageCapacityListener(final CapacityDao capacityDao, final StorageManager storageMgr) {
        this._capacityDao = capacityDao;
        this._storageMgr = storageMgr;
    }

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
    public void processConnect(final Host server, final StartupCommand startup, final boolean forRebalance) throws ConnectionException {

        if (!(startup instanceof StartupStorageCommand)) {
            return;
        }

        final StartupStorageCommand ssCmd = (StartupStorageCommand) startup;
        if (ssCmd.getResourceType() == StorageResourceType.STORAGE_HOST) {
            final BigDecimal overProvFactor = BigDecimal.valueOf(CapacityManager.StorageOverprovisioningFactor.value());
            final CapacityVO capacity =
                    new CapacityVO(server.getId(), server.getDataCenterId(), server.getPodId(), server.getClusterId(), 0L, (overProvFactor.multiply(new BigDecimal(
                            server.getTotalSize()))).longValue(), Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED);
            _capacityDao.persist(capacity);
        }
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
