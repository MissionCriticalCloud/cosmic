package org.apache.cloudstack.storage.helper;

import com.cloud.agent.api.VMSnapshotTO;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.snapshot.VMSnapshot;
import com.cloud.vm.snapshot.VMSnapshotVO;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.to.VolumeObjectTO;
import org.apache.cloudstack.storage.vmsnapshot.VMSnapshotHelper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VMSnapshotHelperImpl implements VMSnapshotHelper {
    @Inject
    VMSnapshotDao _vmSnapshotDao;
    @Inject
    UserVmDao userVmDao;
    @Inject
    HostDao hostDao;
    @Inject
    VolumeDao volumeDao;
    @Inject
    PrimaryDataStoreDao primaryDataStoreDao;
    @Inject
    VolumeDataFactory volumeDataFactory;

    StateMachine2<VMSnapshot.State, VMSnapshot.Event, VMSnapshot> _vmSnapshottateMachine;

    public VMSnapshotHelperImpl() {
        _vmSnapshottateMachine = VMSnapshot.State.getStateMachine();
    }

    @Override
    public boolean vmSnapshotStateTransitTo(final VMSnapshot vsnp, final VMSnapshot.Event event) throws NoTransitionException {
        return _vmSnapshottateMachine.transitTo(vsnp, event, null, _vmSnapshotDao);
    }

    @Override
    public Long pickRunningHost(final Long vmId) {
        final UserVmVO vm = userVmDao.findById(vmId);
        // use VM's host if VM is running
        if (vm.getState() == VirtualMachine.State.Running) {
            return vm.getHostId();
        }

        // check if lastHostId is available
        if (vm.getLastHostId() != null) {
            final HostVO lastHost = hostDao.findByIdIncludingRemoved(vm.getLastHostId());
            if (lastHost.getStatus() == com.cloud.host.Status.Up && !lastHost.isInMaintenanceStates()) {
                return lastHost.getId();
            }
        }

        final List<VolumeVO> listVolumes = volumeDao.findByInstance(vmId);
        if (listVolumes == null || listVolumes.size() == 0) {
            throw new InvalidParameterValueException("vmInstance has no volumes");
        }
        final VolumeVO volume = listVolumes.get(0);
        final Long poolId = volume.getPoolId();
        if (poolId == null) {
            throw new InvalidParameterValueException("pool id is not found");
        }
        final StoragePoolVO storagePool = primaryDataStoreDao.findById(poolId);
        if (storagePool == null) {
            throw new InvalidParameterValueException("storage pool is not found");
        }
        final List<HostVO> listHost =
                hostDao.listAllUpAndEnabledNonHAHosts(Host.Type.Routing, storagePool.getClusterId(), storagePool.getPodId(), storagePool.getDataCenterId(), null);
        if (listHost == null || listHost.size() == 0) {
            throw new InvalidParameterValueException("no host in up state is found");
        }
        return listHost.get(0).getId();
    }

    @Override
    public List<VolumeObjectTO> getVolumeTOList(final Long vmId) {
        final List<VolumeObjectTO> volumeTOs = new ArrayList<>();
        final List<VolumeVO> volumeVos = volumeDao.findByInstance(vmId);
        VolumeInfo volumeInfo = null;
        for (final VolumeVO volume : volumeVos) {
            volumeInfo = volumeDataFactory.getVolume(volume.getId());

            volumeTOs.add((VolumeObjectTO) volumeInfo.getTO());
        }
        return volumeTOs;
    }

    @Override
    public VMSnapshotTO getSnapshotWithParents(final VMSnapshotVO snapshot) {
        final Map<Long, VMSnapshotVO> snapshotMap = new HashMap<>();
        final List<VMSnapshotVO> allSnapshots = _vmSnapshotDao.findByVm(snapshot.getVmId());
        for (final VMSnapshotVO vmSnapshotVO : allSnapshots) {
            snapshotMap.put(vmSnapshotVO.getId(), vmSnapshotVO);
        }

        VMSnapshotTO currentTO = convert2VMSnapshotTO(snapshot);
        final VMSnapshotTO result = currentTO;
        VMSnapshotVO current = snapshot;
        while (current.getParent() != null) {
            final VMSnapshotVO parent = snapshotMap.get(current.getParent());
            if (parent == null) {
                break;
            }
            currentTO.setParent(convert2VMSnapshotTO(parent));
            current = snapshotMap.get(current.getParent());
            currentTO = currentTO.getParent();
        }
        return result;
    }

    private VMSnapshotTO convert2VMSnapshotTO(final VMSnapshotVO vo) {
        return new VMSnapshotTO(vo.getId(), vo.getName(), vo.getType(), vo.getCreated().getTime(), vo.getDescription(), vo.getCurrent(), null, true);
    }
}
