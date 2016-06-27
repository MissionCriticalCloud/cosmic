package com.cloud.hypervisor;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.GuestOSHypervisorVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.GuestOSHypervisorDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.template.VirtualMachineTemplate.BootloaderType;
import com.cloud.utils.Pair;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.hypervisor.xenserver.XenserverConfigs;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.DettachCommand;
import org.apache.cloudstack.storage.command.StorageSubSystemCommand;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XenServerGuru extends HypervisorGuruBase implements HypervisorGuru, Configurable {
    static final ConfigKey<Integer> MaxNumberOfVCPUSPerVM = new ConfigKey<>("Advanced", Integer.class, "xen.vm.vcpu.max", "16",
            "Maximum number of VCPUs that VM can get in XenServer.", true, ConfigKey.Scope.Cluster);
    private final Logger LOGGER = LoggerFactory.getLogger(XenServerGuru.class);
    @Inject
    GuestOSDao _guestOsDao;
    @Inject
    GuestOSHypervisorDao _guestOsHypervisorDao;
    @Inject
    EndPointSelector endPointSelector;
    @Inject
    HostDao hostDao;
    @Inject
    VolumeDao _volumeDao;
    @Inject
    PrimaryDataStoreDao _storagePoolDao;
    @Inject
    VolumeDataFactory _volFactory;
    @Inject
    UserVmDao _userVmDao;

    protected XenServerGuru() {
        super();
    }

    @Override
    public Pair<Boolean, Long> getCommandHostDelegation(final long hostId, final Command cmd) {
        LOGGER.debug("getCommandHostDelegation: " + cmd.getClass());
        if (cmd instanceof StorageSubSystemCommand) {
            final StorageSubSystemCommand c = (StorageSubSystemCommand) cmd;
            c.setExecuteInSequence(true);
        }
        if (cmd instanceof CopyCommand) {
            final CopyCommand cpyCommand = (CopyCommand) cmd;
            final DataTO srcData = cpyCommand.getSrcTO();
            final DataTO destData = cpyCommand.getDestTO();

            if (srcData.getHypervisorType() == HypervisorType.XenServer && srcData.getObjectType() == DataObjectType.SNAPSHOT &&
                    destData.getObjectType() == DataObjectType.TEMPLATE) {
                final DataStoreTO srcStore = srcData.getDataStore();
                final DataStoreTO destStore = destData.getDataStore();
                if (srcStore instanceof NfsTO && destStore instanceof NfsTO) {
                    HostVO host = hostDao.findById(hostId);
                    final EndPoint ep = endPointSelector.selectHypervisorHost(new ZoneScope(host.getDataCenterId()));
                    host = hostDao.findById(ep.getId());
                    hostDao.loadDetails(host);
                    final String hypervisorVersion = host.getHypervisorVersion();
                    final String snapshotHotFixVersion = host.getDetail(XenserverConfigs.XS620HotFix);
                    if (hypervisorVersion != null && !hypervisorVersion.equalsIgnoreCase("6.1.0")) {
                        if (!(hypervisorVersion.equalsIgnoreCase("6.2.0") &&
                                !(snapshotHotFixVersion != null && snapshotHotFixVersion.equalsIgnoreCase(XenserverConfigs.XSHotFix62ESP1004)))) {
                            return new Pair<>(Boolean.TRUE, new Long(ep.getId()));
                        }
                    }
                }
            }
        }
        return new Pair<>(Boolean.FALSE, new Long(hostId));
    }

    @Override
    public HypervisorType getHypervisorType() {
        return HypervisorType.XenServer;
    }

    @Override
    public List<Command> finalizeExpungeVolumes(final VirtualMachine vm) {
        final List<Command> commands = new ArrayList<>();

        final List<VolumeVO> volumes = _volumeDao.findByInstance(vm.getId());

        // it's OK in this case to send a detach command to the host for a root volume as this
        // will simply lead to the SR that supports the root volume being removed
        if (volumes != null) {
            for (final VolumeVO volume : volumes) {
                final StoragePoolVO storagePool = _storagePoolDao.findById(volume.getPoolId());

                // storagePool should be null if we are expunging a volume that was never
                // attached to a VM that was started (the "trick" for storagePool to be null
                // is that none of the VMs this volume may have been attached to were ever started,
                // so the volume was never assigned to a storage pool)
                if (storagePool != null && storagePool.isManaged()) {
                    final DataTO volTO = _volFactory.getVolume(volume.getId()).getTO();
                    final DiskTO disk = new DiskTO(volTO, volume.getDeviceId(), volume.getPath(), volume.getVolumeType());

                    final DettachCommand cmd = new DettachCommand(disk, vm.getInstanceName());

                    cmd.setManaged(true);

                    cmd.setStorageHost(storagePool.getHostAddress());
                    cmd.setStoragePort(storagePool.getPort());

                    cmd.set_iScsiName(volume.get_iScsiName());

                    commands.add(cmd);
                }
            }
        }

        return commands;
    }

    @Override
    public VirtualMachineTO implement(final VirtualMachineProfile vm) {
        BootloaderType bt = BootloaderType.PyGrub;
        if (vm.getBootLoaderType() == BootloaderType.CD) {
            bt = vm.getBootLoaderType();
        }
        final VirtualMachineTO to = toVirtualMachineTO(vm);
        final UserVmVO userVmVO = _userVmDao.findById(vm.getId());
        if (userVmVO != null) {
            final HostVO host = hostDao.findById(userVmVO.getHostId());
            if (host != null) {
                to.setVcpuMaxLimit(MaxNumberOfVCPUSPerVM.valueIn(host.getClusterId()));
            }
        }

        to.setBootloader(bt);

        // Determine the VM's OS description
        final GuestOSVO guestOS = _guestOsDao.findByIdIncludingRemoved(vm.getVirtualMachine().getGuestOSId());
        to.setOs(guestOS.getDisplayName());
        final HostVO host = hostDao.findById(vm.getVirtualMachine().getHostId());
        GuestOSHypervisorVO guestOsMapping = null;
        if (host != null) {
            guestOsMapping = _guestOsHypervisorDao.findByOsIdAndHypervisor(guestOS.getId(), getHypervisorType().toString(), host.getHypervisorVersion());
        }
        if (guestOsMapping == null || host == null) {
            to.setPlatformEmulator(null);
        } else {
            to.setPlatformEmulator(guestOsMapping.getGuestOsName());
        }

        return to;
    }

    @Override
    public Map<String, String> getClusterSettings(final long vmId) {
        return null;
    }

    @Override
    public boolean trackVmHostChange() {
        return true;
    }

    @Override
    public String getConfigComponentName() {
        return XenServerGuru.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{MaxNumberOfVCPUSPerVM};
    }
}
