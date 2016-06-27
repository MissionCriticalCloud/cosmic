package com.cloud.hypervisor.ovm3.resources;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.HypervisorGuru;
import com.cloud.hypervisor.HypervisorGuruBase;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.utils.Pair;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ovm3HypervisorGuru extends HypervisorGuruBase implements HypervisorGuru {

    private final Logger logger = LoggerFactory.getLogger(Ovm3HypervisorGuru.class);
    @Inject
    GuestOSDao guestOsDao;
    @Inject
    EndPointSelector endPointSelector;
    @Inject
    HostDao hostDao;

    protected Ovm3HypervisorGuru() {
        super();
    }

    @Override
    public HypervisorType getHypervisorType() {
        return HypervisorType.Ovm3;
    }

    @Override
    public VirtualMachineTO implement(final VirtualMachineProfile vm) {
        final VirtualMachineTO to = toVirtualMachineTO(vm);
        to.setBootloader(vm.getBootLoaderType());

        // Determine the VM's OS description
        final GuestOSVO guestOs = guestOsDao.findById(vm.getVirtualMachine().getGuestOSId());
        to.setOs(guestOs.getDisplayName());

        return to;
    }

    @Override
    public boolean trackVmHostChange() {
        return true;
    }

    /*
     * I dislike the notion of having to place this here, and not being able to just override
     *
     * (non-Javadoc)
     *
     * @see com.cloud.hypervisor.HypervisorGuruBase#getCommandHostDelegation(long, com.cloud.agent.api.Command)
     */
    @Override
    public Pair<Boolean, Long> getCommandHostDelegation(final long hostId, final Command cmd) {
        logger.debug("getCommandHostDelegation: " + cmd.getClass());
        if (cmd instanceof StorageSubSystemCommand) {
            final StorageSubSystemCommand c = (StorageSubSystemCommand) cmd;
            c.setExecuteInSequence(true);
        }
        if (cmd instanceof CopyCommand) {
            final CopyCommand cpyCommand = (CopyCommand) cmd;
            final DataTO srcData = cpyCommand.getSrcTO();
            final DataTO destData = cpyCommand.getDestTO();

            if (HypervisorType.Ovm3.equals(srcData.getHypervisorType())
                    && srcData.getObjectType() == DataObjectType.SNAPSHOT
                    && destData.getObjectType() == DataObjectType.TEMPLATE) {
                logger.debug("Snapshot to Template: " + cmd);
                final DataStoreTO srcStore = srcData.getDataStore();
                final DataStoreTO destStore = destData.getDataStore();
                if (srcStore instanceof NfsTO && destStore instanceof NfsTO) {
                    final HostVO host = hostDao.findById(hostId);
                    final EndPoint ep = endPointSelector.selectHypervisorHost(new ZoneScope(host.getDataCenterId()));
                    if (ep != null) {
                        return new Pair<>(Boolean.TRUE, Long.valueOf(ep.getId()));
                    }
                }
            }
        }
        return new Pair<>(Boolean.FALSE, Long.valueOf(hostId));
    }
}
