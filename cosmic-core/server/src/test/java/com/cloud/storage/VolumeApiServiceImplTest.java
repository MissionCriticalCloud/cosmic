package com.cloud.storage;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.command.user.volume.CreateVolumeCmd;
import com.cloud.api.command.user.volume.DetachVolumeCmd;
import com.cloud.context.CallContext;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.engine.subsystem.api.storage.SnapshotInfo;
import com.cloud.engine.subsystem.api.storage.VolumeDataFactory;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.engine.subsystem.api.storage.VolumeService;
import com.cloud.framework.jobs.AsyncJobExecutionContext;
import com.cloud.framework.jobs.AsyncJobManager;
import com.cloud.framework.jobs.dao.AsyncJobJoinMapDao;
import com.cloud.framework.jobs.impl.AsyncJobVO;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.acl.ControlledEntity;
import com.cloud.legacymodel.configuration.Resource;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.storage.StorageProvisioningType;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.MaintenancePolicy;
import com.cloud.model.enumeration.OptimiseFor;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.model.enumeration.VolumeType;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.ResourceLimitService;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class VolumeApiServiceImplTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Inject
    VolumeApiServiceImpl _svc = new VolumeApiServiceImpl();
    @Mock
    VolumeDao _volumeDao;
    @Mock
    AccountManager _accountMgr;
    @Mock
    UserVmDao _userVmDao;
    @Mock
    PrimaryDataStoreDao _storagePoolDao;
    @Mock
    VMSnapshotDao _vmSnapshotDao;
    @Mock
    AsyncJobManager _jobMgr;
    @Mock
    AsyncJobJoinMapDao _joinMapDao;
    @Mock
    VolumeDataFactory _volFactory;
    @Mock
    VMInstanceDao _vmInstanceDao;
    @Mock
    DataCenterDao _dcDao;
    @Mock
    ResourceLimitService _resourceLimitMgr;
    @Mock
    AccountDao _accountDao;
    @Mock
    HostDao _hostDao;
    @Mock
    VolumeInfo volumeInfoMock;
    @Mock
    SnapshotInfo snapshotInfoMock;
    @Mock
    VolumeService volService;
    @Mock
    CreateVolumeCmd createVol;
    DetachVolumeCmd detachCmd = new DetachVolumeCmd();
    Class<?> _detachCmdClass = detachCmd.getClass();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        _svc._volsDao = _volumeDao;
        _svc._accountMgr = _accountMgr;
        _svc._userVmDao = _userVmDao;
        _svc._storagePoolDao = _storagePoolDao;
        _svc._vmSnapshotDao = _vmSnapshotDao;
        _svc._vmInstanceDao = _vmInstanceDao;
        _svc._jobMgr = _jobMgr;
        _svc.volFactory = _volFactory;
        _svc.volService = volService;
        _svc._dcDao = _dcDao;
        _svc._resourceLimitMgr = _resourceLimitMgr;
        _svc._accountDao = _accountDao;
        _svc._hostDao = _hostDao;

        // mock caller context
        final AccountVO account = new AccountVO("admin", 1L, "networkDomain", Account.ACCOUNT_TYPE_NORMAL, "uuid");
        final AccountVO account2 = new AccountVO("Account2", 2L, "networkDomain", Account.ACCOUNT_TYPE_NORMAL, "uuid");
        final UserVO user = new UserVO(1, "testuser", "password", "firstname", "lastName", "email", "timezone", UUID.randomUUID().toString(), User.Source.UNKNOWN);
        CallContext.register(user, account);
        // mock async context
        final AsyncJobExecutionContext context = new AsyncJobExecutionContext();
        AsyncJobExecutionContext.init(_svc._jobMgr, _joinMapDao);
        final AsyncJobVO job = new AsyncJobVO();
        context.setJob(job);
        AsyncJobExecutionContext.setCurrentExecutionContext(context);

        final TransactionLegacy txn = TransactionLegacy.open("runVolumeDaoImplTest");
        try {
            // volume of running vm id=1
            final VolumeVO volumeOfRunningVm = new VolumeVO("root", 1L, 1L, 1L, 1L, 1L, "root", "root", StorageProvisioningType.THIN, 1, null,
                    null, "root", VolumeType.ROOT, DiskControllerType.SCSI);
            when(_svc._volsDao.findById(1L)).thenReturn(volumeOfRunningVm);

            final UserVmVO runningVm = new UserVmVO(1L, "vm", "vm", 1, HypervisorType.XenServer, 1L, false,
                    false, 1L, 1L, 1, 1L, null, "vm", null, "Manufacturer", OptimiseFor.Generic, false, "", MaintenancePolicy.LiveMigrate);
            runningVm.setState(State.Running);
            runningVm.setDataCenterId(1L);
            when(_svc._userVmDao.findById(1L)).thenReturn(runningVm);

            // volume of stopped vm id=2
            final VolumeVO volumeOfStoppedVm = new VolumeVO("root", 1L, 1L, 1L, 1L, 2L, "root", "root", StorageProvisioningType.THIN, 1, null,
                    null, "root", VolumeType.ROOT, DiskControllerType.SCSI);
            volumeOfStoppedVm.setPoolId(1L);
            when(_svc._volsDao.findById(2L)).thenReturn(volumeOfStoppedVm);

            final UserVmVO stoppedVm = new UserVmVO(2L, "vm", "vm", 1, HypervisorType.XenServer, 1L, false,
                    false, 1L, 1L, 1, 1L, null, "vm", null, "Manufacturer", OptimiseFor.Generic, false, "", MaintenancePolicy.LiveMigrate);
            stoppedVm.setState(State.Stopped);
            stoppedVm.setDataCenterId(1L);
            when(_svc._userVmDao.findById(2L)).thenReturn(stoppedVm);

            final StoragePoolVO unmanagedPool = new StoragePoolVO();
            when(_svc._storagePoolDao.findById(1L)).thenReturn(unmanagedPool);

            // volume of managed pool id=4
            final StoragePoolVO managedPool = new StoragePoolVO();
            managedPool.setManaged(true);
            when(_svc._storagePoolDao.findById(2L)).thenReturn(managedPool);
            final VolumeVO managedPoolVolume = new VolumeVO("root", 1L, 1L, 1L, 1L, 2L, "root", "root", StorageProvisioningType.THIN, 1, null,
                    null, "root", VolumeType.ROOT, DiskControllerType.SCSI);
            managedPoolVolume.setPoolId(2L);
            when(_svc._volsDao.findById(4L)).thenReturn(managedPoolVolume);

            // non-root non-datadisk volume
            final VolumeInfo volumeWithIncorrectVolumeType = Mockito.mock(VolumeInfo.class);
            when(volumeWithIncorrectVolumeType.getId()).thenReturn(5L);
            when(volumeWithIncorrectVolumeType.getVolumeType()).thenReturn(VolumeType.ISO);
            when(_svc.volFactory.getVolume(5L)).thenReturn(volumeWithIncorrectVolumeType);

            // correct root volume
            final VolumeInfo correctRootVolume = Mockito.mock(VolumeInfo.class);
            when(correctRootVolume.getId()).thenReturn(6L);
            when(correctRootVolume.getDataCenterId()).thenReturn(1L);
            when(correctRootVolume.getVolumeType()).thenReturn(VolumeType.ROOT);
            when(correctRootVolume.getInstanceId()).thenReturn(null);
            when(_svc.volFactory.getVolume(6L)).thenReturn(correctRootVolume);

            final VolumeVO correctRootVolumeVO = new VolumeVO("root", 1L, 1L, 1L, 1L, 2L, "root", "root", StorageProvisioningType.THIN, 1, null,
                    null, "root", VolumeType.ROOT, DiskControllerType.SCSI);
            when(_svc._volsDao.findById(6L)).thenReturn(correctRootVolumeVO);

            // managed root volume
            final VolumeInfo managedVolume = Mockito.mock(VolumeInfo.class);
            when(managedVolume.getId()).thenReturn(7L);
            when(managedVolume.getDataCenterId()).thenReturn(1L);
            when(managedVolume.getVolumeType()).thenReturn(VolumeType.ROOT);
            when(managedVolume.getInstanceId()).thenReturn(null);
            when(managedVolume.getPoolId()).thenReturn(2L);
            when(_svc.volFactory.getVolume(7L)).thenReturn(managedVolume);

            final VolumeVO managedVolume1 = new VolumeVO("root", 1L, 1L, 1L, 1L, 2L, "root", "root", StorageProvisioningType.THIN, 1, null,
                    null, "root", VolumeType.ROOT, DiskControllerType.SCSI);
            managedVolume1.setPoolId(2L);
            managedVolume1.setDataCenterId(1L);
            when(_svc._volsDao.findById(7L)).thenReturn(managedVolume1);

            // vm having root volume
            final UserVmVO vmHavingRootVolume = new UserVmVO(4L, "vm", "vm", 1, HypervisorType.XenServer, 1L, false,
                    false, 1L, 1L, 1, 1L, null, "vm", null, "Manufacturer", OptimiseFor.Generic, false, "", MaintenancePolicy.LiveMigrate);
            vmHavingRootVolume.setState(State.Stopped);
            vmHavingRootVolume.setDataCenterId(1L);
            when(_svc._userVmDao.findById(4L)).thenReturn(vmHavingRootVolume);
            final List<VolumeVO> vols = new ArrayList<>();
            vols.add(new VolumeVO());
            when(_svc._volsDao.findByInstanceAndDeviceId(4L, 0L)).thenReturn(vols);

            // volume in uploaded state
            final VolumeInfo uploadedVolume = Mockito.mock(VolumeInfo.class);
            when(uploadedVolume.getId()).thenReturn(8L);
            when(uploadedVolume.getDataCenterId()).thenReturn(1L);
            when(uploadedVolume.getVolumeType()).thenReturn(VolumeType.ROOT);
            when(uploadedVolume.getInstanceId()).thenReturn(null);
            when(uploadedVolume.getPoolId()).thenReturn(1L);
            when(uploadedVolume.getState()).thenReturn(Volume.State.Uploaded);
            when(_svc.volFactory.getVolume(8L)).thenReturn(uploadedVolume);

            final VolumeVO upVolume = new VolumeVO("root", 1L, 1L, 1L, 1L, 2L, "root", "root", StorageProvisioningType.THIN, 1, null,
                    null, "root", VolumeType.ROOT, DiskControllerType.SCSI);
            upVolume.setPoolId(1L);
            upVolume.setDataCenterId(1L);
            upVolume.setState(Volume.State.Uploaded);
            when(_svc._volsDao.findById(8L)).thenReturn(upVolume);

            // helper dao methods mock
            when(_svc._vmSnapshotDao.findByVm(any(Long.class))).thenReturn(new ArrayList<>());
            when(_svc._vmInstanceDao.findById(any(Long.class))).thenReturn(stoppedVm);
        } finally {
            txn.close("runVolumeDaoImplTest");
        }

        // helper methods mock
        doNothing().when(_svc._accountMgr).checkAccess(any(Account.class), any(AccessType.class), any(Boolean.class), any(ControlledEntity.class));
        doNothing().when(_svc._jobMgr).updateAsyncJobAttachment(any(Long.class), any(String.class), any(Long.class));
        when(_svc._jobMgr.submitAsyncJob(any(AsyncJobVO.class), any(String.class), any(Long.class))).thenReturn(1L);
    }

    /**
     * TESTS FOR DETACH ROOT VOLUME, COUNT=4
     *
     * @throws Exception
     */

    @Test(expected = InvalidParameterValueException.class)
    public void testDetachVolumeFromRunningVm() throws NoSuchFieldException, IllegalAccessException {
        final Field dedicateIdField = _detachCmdClass.getDeclaredField("id");
        dedicateIdField.setAccessible(true);
        dedicateIdField.set(detachCmd, 1L);
        _svc.detachVolumeFromVM(detachCmd);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testDetachVolumeOfManagedDataStore() throws NoSuchFieldException, IllegalAccessException {
        final Field dedicateIdField = _detachCmdClass.getDeclaredField("id");
        dedicateIdField.setAccessible(true);
        dedicateIdField.set(detachCmd, 4L);
        _svc.detachVolumeFromVM(detachCmd);
    }

    @Test
    public void testDetachVolumeFromStoppedXenVm() throws NoSuchFieldException, IllegalAccessException {
        thrown.expect(NullPointerException.class);
        final Field dedicateIdField = _detachCmdClass.getDeclaredField("id");
        dedicateIdField.setAccessible(true);
        dedicateIdField.set(detachCmd, 2L);
        _svc.detachVolumeFromVM(detachCmd);
    }

    /**
     * TESTS FOR ATTACH ROOT VOLUME, COUNT=7
     */

    // Negative test - try to attach non-root non-datadisk volume
    @Test(expected = InvalidParameterValueException.class)
    public void attachIncorrectDiskType() throws NoSuchFieldException, IllegalAccessException {
        _svc.attachVolumeToVM(1L, 5L, 0L, DiskControllerType.SCSI);
    }

    // Negative test - attach root volume to running vm
    @Test(expected = InvalidParameterValueException.class)
    public void attachRootDiskToRunningVm() throws NoSuchFieldException, IllegalAccessException {
        _svc.attachVolumeToVM(1L, 6L, 0L, DiskControllerType.SCSI);
    }

    // Negative test - attach root volume from the managed data store
    @Test(expected = InvalidParameterValueException.class)
    public void attachRootDiskOfManagedDataStore() throws NoSuchFieldException, IllegalAccessException {
        _svc.attachVolumeToVM(2L, 7L, 0L, DiskControllerType.SCSI);
    }

    // Negative test - root volume can't be attached to the vm already having a root volume attached
    @Test(expected = InvalidParameterValueException.class)
    public void attachRootDiskToVmHavingRootDisk() throws NoSuchFieldException, IllegalAccessException {
        _svc.attachVolumeToVM(4L, 6L, 0L, DiskControllerType.SCSI);
    }

    // Negative test - root volume in uploaded state can't be attached
    @Test(expected = InvalidParameterValueException.class)
    public void attachRootInUploadedState() throws NoSuchFieldException, IllegalAccessException {
        _svc.attachVolumeToVM(2L, 8L, 0L, DiskControllerType.SCSI);
    }

    // Positive test - attach ROOT volume in correct state, to the vm not having root volume attached
    @Test
    public void attachRootVolumePositive() throws NoSuchFieldException, IllegalAccessException {
        thrown.expect(InvalidParameterValueException.class);
        _svc.attachVolumeToVM(2L, 6L, 0L, DiskControllerType.SCSI);
    }

    // volume not Ready
    @Test(expected = InvalidParameterValueException.class)
    public void testTakeSnapshotF1() throws ResourceAllocationException {
        when(_volFactory.getVolume(anyLong())).thenReturn(volumeInfoMock);
        when(volumeInfoMock.getState()).thenReturn(Volume.State.Allocated);
        _svc.takeSnapshot(5L, Snapshot.MANUAL_POLICY_ID, 3L, null, false);
    }

    @Test
    public void testTakeSnapshotF2() throws ResourceAllocationException {
        when(_volFactory.getVolume(anyLong())).thenReturn(volumeInfoMock);
        when(volumeInfoMock.getState()).thenReturn(Volume.State.Ready);
        when(volumeInfoMock.getInstanceId()).thenReturn(null);
        when(volService.takeSnapshot(Mockito.any(VolumeInfo.class))).thenReturn(snapshotInfoMock);
        _svc.takeSnapshot(5L, Snapshot.MANUAL_POLICY_ID, 3L, null, false);
    }

    @Test
    public void testNullGetVolumeNameFromCmd() {
        when(createVol.getVolumeName()).thenReturn(null);
        Assert.assertNotNull(_svc.getVolumeNameFromCommand(createVol));
    }

    @Test
    public void testEmptyGetVolumeNameFromCmd() {
        when(createVol.getVolumeName()).thenReturn("");
        Assert.assertNotNull(_svc.getVolumeNameFromCommand(createVol));
    }

    @Test
    public void testBlankGetVolumeNameFromCmd() {
        when(createVol.getVolumeName()).thenReturn("   ");
        Assert.assertNotNull(_svc.getVolumeNameFromCommand(createVol));
    }

    @Test
    public void testNonEmptyGetVolumeNameFromCmd() {
        when(createVol.getVolumeName()).thenReturn("abc");
        Assert.assertSame(_svc.getVolumeNameFromCommand(createVol), "abc");
    }

    //The resource limit check for primary storage should not be skipped for Volume in 'Uploaded' state.
    @Test
    public void testResourceLimitCheckForUploadedVolume() throws NoSuchFieldException, IllegalAccessException, ResourceAllocationException {
        doThrow(new ResourceAllocationException("primary storage resource limit check failed", Resource.ResourceType.primary_storage)).when(_svc._resourceLimitMgr).checkResourceLimit(any(AccountVO
                .class), any(Resource.ResourceType.class), any(Long.class));
        final UserVmVO vm = Mockito.mock(UserVmVO.class);
        final VolumeInfo volumeToAttach = Mockito.mock(VolumeInfo.class);
        when(volumeToAttach.getId()).thenReturn(9L);
        when(volumeToAttach.getDataCenterId()).thenReturn(34L);
        when(volumeToAttach.getVolumeType()).thenReturn(VolumeType.DATADISK);
        when(volumeToAttach.getInstanceId()).thenReturn(null);
        when(_userVmDao.findById(anyLong())).thenReturn(vm);
        when(vm.getType()).thenReturn(VirtualMachineType.User);
        when(vm.getState()).thenReturn(State.Running);
        when(vm.getDataCenterId()).thenReturn(34L);
        when(_svc._volsDao.findByInstanceAndType(anyLong(), any(VolumeType.class))).thenReturn(new ArrayList(10));
        when(_svc.volFactory.getVolume(9L)).thenReturn(volumeToAttach);
        when(volumeToAttach.getState()).thenReturn(Volume.State.Uploaded);
        final DataCenterVO zoneWithDisabledLocalStorage = Mockito.mock(DataCenterVO.class);
        when(_svc._dcDao.findById(anyLong())).thenReturn(zoneWithDisabledLocalStorage);
        try {
            _svc.attachVolumeToVM(2L, 9L, null, DiskControllerType.SCSI);
        } catch (final InvalidParameterValueException e) {
            Assert.assertEquals(e.getMessage(), ("primary storage resource limit check failed"));
        }
    }

    @After
    public void tearDown() {
        CallContext.unregister();
    }
}
