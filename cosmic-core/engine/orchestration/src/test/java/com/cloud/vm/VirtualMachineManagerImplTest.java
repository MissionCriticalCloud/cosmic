package com.cloud.vm;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.agent.AgentManager;
import com.cloud.agent.manager.Commands;
import com.cloud.api.command.user.vm.RestoreVMCmd;
import com.cloud.capacity.CapacityManager;
import com.cloud.context.CallContext;
import com.cloud.dao.EntityManager;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterDetailsVO;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.deploy.DeploymentPlanningManager;
import com.cloud.engine.orchestration.service.NetworkOrchestrationService;
import com.cloud.engine.orchestration.service.VolumeOrchestrationService;
import com.cloud.framework.config.ConfigDepot;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.HypervisorGuru;
import com.cloud.hypervisor.HypervisorGuruManager;
import com.cloud.legacymodel.communication.answer.CheckVirtualMachineAnswer;
import com.cloud.legacymodel.communication.answer.MigrateWithStorageAnswer;
import com.cloud.legacymodel.communication.answer.MigrateWithStorageCompleteAnswer;
import com.cloud.legacymodel.communication.answer.MigrateWithStorageReceiveAnswer;
import com.cloud.legacymodel.communication.answer.MigrateWithStorageSendAnswer;
import com.cloud.legacymodel.communication.answer.PrepareForMigrationAnswer;
import com.cloud.legacymodel.communication.answer.ScaleVmAnswer;
import com.cloud.legacymodel.communication.answer.StartAnswer;
import com.cloud.legacymodel.communication.answer.StopAnswer;
import com.cloud.legacymodel.communication.command.CheckVirtualMachineCommand;
import com.cloud.legacymodel.communication.command.Command;
import com.cloud.legacymodel.communication.command.MigrateWithStorageCommand;
import com.cloud.legacymodel.communication.command.MigrateWithStorageCompleteCommand;
import com.cloud.legacymodel.communication.command.MigrateWithStorageReceiveCommand;
import com.cloud.legacymodel.communication.command.MigrateWithStorageSendCommand;
import com.cloud.legacymodel.communication.command.PrepareForMigrationCommand;
import com.cloud.legacymodel.communication.command.ScaleVmCommand;
import com.cloud.legacymodel.communication.command.StartCommand;
import com.cloud.legacymodel.communication.command.StopCommand;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.ManagementServerException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.exceptions.VirtualMachineMigrationException;
import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.to.DiskTO;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.Event;
import com.cloud.legacymodel.vm.VirtualMachine.PowerState;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.model.enumeration.ComplianceStatus;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.StorageProvisioningType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.offering.DiskOfferingInfo;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.StoragePoolHostVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.StoragePoolHostDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;
import com.cloud.vm.snapshot.VMSnapshotManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;
import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class VirtualMachineManagerImplTest {

    @Spy
    VirtualMachineManagerImpl _vmMgr = spy(new VirtualMachineManagerImpl());
    @Mock
    VolumeOrchestrationService _storageMgr;
    @Mock
    Account _account;
    @Mock
    CapacityManager _capacityMgr;
    @Mock
    AgentManager _agentMgr;
    @Mock
    AccountDao _accountDao;
    @Mock
    ConfigurationDao _configDao;
    @Mock
    HostDao _hostDao;
    @Mock
    UserDao _userDao;
    @Mock
    UserVmDao _vmDao;
    @Mock
    ItWorkDao _workDao;
    @Mock
    VMInstanceDao _vmInstanceDao;
    @Mock
    VMTemplateDao _templateDao;
    @Mock
    VolumeDao _volsDao;
    @Mock
    RestoreVMCmd _restoreVMCmd;
    @Mock
    AccountVO _accountMock;
    @Mock
    UserVO _userMock;
    @Mock
    UserVmVO _vmMock;
    @Mock
    VMInstanceVO _vmInstance;
    @Mock
    HostVO _host;
    @Mock
    HostPodVO _pod;
    @Mock
    ClusterVO _cluster;
    @Mock
    VMTemplateVO _templateMock;
    @Mock
    VolumeVO _volumeMock;
    @Mock
    List<VolumeVO> _rootVols;
    @Mock
    ItWorkVO _work;
    @Mock
    HostVO hostVO;
    @Mock
    UserVmDetailVO _vmDetailVO;

    @Mock
    ClusterDao _clusterDao;
    @Mock
    ClusterDetailsDao _clusterDetailsDao;
    @Mock
    HostPodDao _podDao;
    @Mock
    DataCenterDao _dcDao;
    @Mock
    DiskOfferingDao _diskOfferingDao;
    @Mock
    PrimaryDataStoreDao _storagePoolDao;
    @Mock
    UserVmDetailsDao _vmDetailsDao;
    @Mock
    StoragePoolHostDao _poolHostDao;
    @Mock
    ServiceOfferingDao _offeringDao;
    @Mock
    NetworkOrchestrationService _networkMgr;
    @Mock
    HypervisorGuruManager _hvGuruMgr;
    @Mock
    VMSnapshotManager _vmSnapshotMgr;
    @Mock
    DeploymentPlanningManager _dpMgr;

    // Mock objects for vm migration with storage test.
    @Mock
    DiskOfferingVO _diskOfferingMock;
    @Mock
    StoragePoolVO _srcStoragePoolMock;
    @Mock
    StoragePoolVO _destStoragePoolMock;
    @Mock
    HostVO _srcHostMock;
    @Mock
    HostVO _destHostMock;
    @Mock
    Map<Long, Long> _volumeToPoolMock;
    @Mock
    EntityManager _entityMgr;
    @Mock
    ConfigDepot _configDepot;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        _vmMgr._templateDao = _templateDao;
        _vmMgr._volsDao = _volsDao;
        _vmMgr.volumeMgr = _storageMgr;
        _vmMgr._capacityMgr = _capacityMgr;
        _vmMgr._hostDao = _hostDao;
        _vmMgr._nodeId = 1L;
        _vmMgr._workDao = _workDao;
        _vmMgr._agentMgr = _agentMgr;
        _vmMgr._podDao = _podDao;
        _vmMgr._clusterDao = _clusterDao;
        _vmMgr._clusterDetailsDao = _clusterDetailsDao;
        _vmMgr._dcDao = _dcDao;
        _vmMgr._diskOfferingDao = _diskOfferingDao;
        _vmMgr._storagePoolDao = _storagePoolDao;
        _vmMgr._poolHostDao = _poolHostDao;
        _vmMgr._offeringDao = _offeringDao;
        _vmMgr._networkMgr = _networkMgr;
        _vmMgr._hvGuruMgr = _hvGuruMgr;
        _vmMgr._vmSnapshotMgr = _vmSnapshotMgr;
        _vmMgr._vmDao = _vmInstanceDao;
        _vmMgr._uservmDetailsDao = _vmDetailsDao;
        _vmMgr._entityMgr = _entityMgr;
        _vmMgr._configDepot = _configDepot;
        _vmMgr._dpMgr = _dpMgr;

        final AccountVO account = new AccountVO("admin", 5L, "networkDomain", Account.ACCOUNT_TYPE_NORMAL, "uuid");
        final UserVO user = new UserVO(1, "testuser", "password", "firstname", "lastName", "email", "timezone", UUID.randomUUID().toString(), User.Source.UNKNOWN);
        CallContext.register(user, account);

        when(_vmMock.getId()).thenReturn(314l);
        when(_vmMock.getServiceOfferingId()).thenReturn(2L);
        when(_vmMock.getTemplateId()).thenReturn(1L);
        when(_vmMock.getType()).thenReturn(VirtualMachineType.User);
        when(_vmMock.getHypervisorType()).thenReturn(HypervisorType.KVM);
        when(_vmInstance.getId()).thenReturn(1L);
        when(_vmInstance.getServiceOfferingId()).thenReturn(2L);
        when(_vmInstance.getInstanceName()).thenReturn("myVm");
        when(_vmInstance.getHostId()).thenReturn(2L);
        when(_vmInstance.getType()).thenReturn(VirtualMachineType.User);
        when(_host.getId()).thenReturn(1L);
        when(_hostDao.findById(anyLong())).thenReturn(null);
        when(_pod.getId()).thenReturn(1L);
        when(_cluster.getId()).thenReturn(1L);
        when(_entityMgr.findById(eq(ServiceOffering.class), anyLong())).thenReturn(getSvcoffering(512));
        when(_entityMgr.findById(eq(Account.class), anyLong())).thenReturn(account);
        when(_entityMgr.findByIdIncludingRemoved(eq(VirtualMachineTemplate.class), anyLong())).thenReturn(_templateMock);
        when(_workDao.persist(any(ItWorkVO.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(_workDao.update("1", _work)).thenReturn(true);
        when(_workDao.findById(any(String.class))).thenReturn(_work);
        when(_work.getId()).thenReturn("1");
        doNothing().when(_work).setStep(ItWorkVO.Step.Done);
        when(_vmInstanceDao.findById(anyLong())).thenReturn(_vmMock);
        when(_vmInstanceDao.findByUuid(any(String.class))).thenReturn(_vmMock);
        when(_vmInstanceDao.findVMByInstanceName(any(String.class))).thenReturn(_vmMock);
        when(_vmInstanceDao.persist(any(VMInstanceVO.class))).thenReturn(_vmMock);
        when(_offeringDao.findById(anyLong(), anyLong())).thenReturn(getSvcoffering(512));
        when(_clusterDetailsDao.findDetail(anyLong(), any(String.class))).thenAnswer(invocation -> new ClusterDetailsVO((long) invocation.getArguments()[0], (String) invocation.getArguments()[1], "1"));
        //doNothing().when(_volsDao).detachVolume(anyLong());
        //when(_work.setStep(ItWorkVO.HaWorkStep.Done)).thenReturn("1");

    }

    private ServiceOfferingVO getSvcoffering(final int ramSize) {

        final String name = "name";
        final String displayText = "displayText";
        final int cpu = 1;
        //int ramSize = 256;

        final boolean ha = false;
        final boolean useLocalStorage = false;

        final ServiceOfferingVO serviceOffering =
                new ServiceOfferingVO(name, cpu, ramSize, null, null, ha, displayText, StorageProvisioningType.THIN, useLocalStorage, false, null, false, null, false);
        return serviceOffering;
    }

    private DeploymentPlan getDeploymentPlan() {
        final DeploymentPlan deploymentPlan = mock(DeploymentPlan.class);
        when(deploymentPlan.getClusterId()).thenReturn(null);
        when(deploymentPlan.getPoolId()).thenReturn(null);

        return deploymentPlan;
    }

    private DiskOfferingInfo getDiskOfferingInfo() {
        final DiskOfferingInfo diskOfferingInfo = new DiskOfferingInfo();
        diskOfferingInfo.setDiskOffering(mock(DiskOfferingVO.class));
        diskOfferingInfo.setSize(1L);

        return diskOfferingInfo;
    }

    @Test(expected = CloudRuntimeException.class)
    public void testScaleVM1() throws Exception {

        final DeployDestination dest = new DeployDestination(null, null, null, _host);
        final long l = 1L;

        when(_vmInstanceDao.findById(anyLong())).thenReturn(_vmInstance);
        _vmMgr.migrateForScale(_vmInstance.getUuid(), l, dest, l);
    }

    @Test(expected = CloudRuntimeException.class)
    public void testScaleVM2() throws Exception {

        new DeployDestination(null, null, null, _host);
        doReturn(3L).when(_vmInstance).getId();
        when(_vmInstanceDao.findById(anyLong())).thenReturn(_vmInstance);
        final ServiceOfferingVO newServiceOffering = getSvcoffering(512);
        doReturn(1L).when(_vmInstance).getHostId();
        doReturn(hostVO).when(_hostDao).findById(1L);
        doReturn(1L).when(_vmInstance).getDataCenterId();
        doReturn(1L).when(hostVO).getClusterId();
        when(CapacityManager.CpuOverprovisioningFactor.valueIn(1L)).thenReturn(1.0f);
        final ScaleVmCommand reconfigureCmd =
                new ScaleVmCommand("myVmName", newServiceOffering.getCpu(), newServiceOffering.getRamSize(), newServiceOffering.getRamSize(), newServiceOffering.getLimitCpuUse());
        new ScaleVmAnswer(reconfigureCmd, true, "details");
        when(_agentMgr.send(2l, reconfigureCmd)).thenReturn(null);
        _vmMgr.reConfigureVm(_vmInstance.getUuid(), getSvcoffering(256), false);
    }

    @Test(expected = CloudRuntimeException.class)
    public void testScaleVM3() throws Exception {

        /*VirtualMachineProfile profile = new VirtualMachineProfileImpl(vm);

        Long srcHostId = vm.getHostId();
        Long oldSvcOfferingId = vm.getServiceOfferingId();
        if (srcHostId == null) {
            throw new CloudRuntimeException("Unable to scale the vm because it doesn't have a host id");
        }*/

        when(_vmInstance.getHostId()).thenReturn(null);
        when(_vmInstanceDao.findById(anyLong())).thenReturn(_vmInstance);
        when(_vmInstanceDao.findByUuid(any(String.class))).thenReturn(_vmInstance);
        final DeploymentPlanner.ExcludeList excludeHostList = new DeploymentPlanner.ExcludeList();
        _vmMgr.findHostAndMigrate(_vmInstance.getUuid(), 2l, excludeHostList);
    }

    // Check migration of a vm with its volumes within a cluster.
    @Test
    public void testMigrateWithVolumeWithinCluster() throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException, OperationTimedoutException {

        initializeMockConfigForMigratingVmWithVolumes();
        when(_srcHostMock.getClusterId()).thenReturn(3L);
        when(_destHostMock.getClusterId()).thenReturn(3L);

        _vmMgr.migrateWithStorage(_vmInstance.getUuid(), _srcHostMock.getId(), _destHostMock.getId(), _volumeToPoolMock);
    }

    private void initializeMockConfigForMigratingVmWithVolumes() throws OperationTimedoutException, ResourceUnavailableException {

        // Mock the source and destination hosts.
        when(_srcHostMock.getId()).thenReturn(5L);
        when(_destHostMock.getId()).thenReturn(6L);
        when(_hostDao.findById(5L)).thenReturn(_srcHostMock);
        when(_hostDao.findById(6L)).thenReturn(_destHostMock);

        // Mock the vm being migrated.
        when(_vmMock.getId()).thenReturn(1L);
        when(_vmMock.getHypervisorType()).thenReturn(HypervisorType.XenServer);
        when(_vmMock.getState()).thenReturn(State.Running).thenReturn(State.Running).thenReturn(State.Migrating).thenReturn(State.Migrating);
        when(_vmMock.getHostId()).thenReturn(5L);
        when(_vmInstance.getId()).thenReturn(1L);
        when(_vmInstance.getServiceOfferingId()).thenReturn(2L);
        when(_vmInstance.getInstanceName()).thenReturn("myVm");
        when(_vmInstance.getHostId()).thenReturn(5L);
        when(_vmInstance.getType()).thenReturn(VirtualMachineType.User);
        when(_vmInstance.getState()).thenReturn(State.Running).thenReturn(State.Running).thenReturn(State.Migrating).thenReturn(State.Migrating);

        // Mock the work item.
        when(_workDao.persist(any(ItWorkVO.class))).thenReturn(_work);
        when(_workDao.update("1", _work)).thenReturn(true);
        when(_work.getId()).thenReturn("1");
        doNothing().when(_work).setStep(ItWorkVO.Step.Done);

        // Mock the vm guru and the user vm object that gets returned.
        _vmMgr._vmGurus = new HashMap<>();
        //        UserVmManagerImpl userVmManager = mock(UserVmManagerImpl.class);
        //        _vmMgr.registerGuru(VirtualMachine.Type.User, userVmManager);

        // Mock the iteration over all the volumes of an instance.
        final Iterator<VolumeVO> volumeIterator = mock(Iterator.class);
        when(_volsDao.findUsableVolumesForInstance(anyLong())).thenReturn(_rootVols);
        when(_rootVols.iterator()).thenReturn(volumeIterator);
        when(volumeIterator.hasNext()).thenReturn(true, false);
        when(volumeIterator.next()).thenReturn(_volumeMock);

        // Mock the disk offering and pool objects for a volume.
        when(_volumeMock.getDiskOfferingId()).thenReturn(5L);
        when(_volumeMock.getPoolId()).thenReturn(200L);
        when(_volumeMock.getId()).thenReturn(5L);
        when(_diskOfferingDao.findById(anyLong())).thenReturn(_diskOfferingMock);
        when(_storagePoolDao.findById(200L)).thenReturn(_srcStoragePoolMock);
        when(_storagePoolDao.findById(201L)).thenReturn(_destStoragePoolMock);

        // Mock the volume to pool mapping.
        when(_volumeToPoolMock.get(5L)).thenReturn(201L);
        when(_destStoragePoolMock.getId()).thenReturn(201L);
        when(_srcStoragePoolMock.getId()).thenReturn(200L);
        when(_destStoragePoolMock.isLocal()).thenReturn(false);
        when(_diskOfferingMock.getUseLocalStorage()).thenReturn(false);
        when(_poolHostDao.findByPoolHost(anyLong(), anyLong())).thenReturn(mock(StoragePoolHostVO.class));

        // Mock hypervisor guru.
        final HypervisorGuru guruMock = mock(HypervisorGuru.class);
        when(_hvGuruMgr.getGuru(HypervisorType.XenServer)).thenReturn(guruMock);

        when(_srcHostMock.getClusterId()).thenReturn(3L);
        when(_destHostMock.getClusterId()).thenReturn(3L);

        // Mock the commands and answers to the agent.
        final PrepareForMigrationAnswer prepAnswerMock = mock(PrepareForMigrationAnswer.class);
        when(prepAnswerMock.getResult()).thenReturn(true);
        when(_agentMgr.send(anyLong(), isA(PrepareForMigrationCommand.class))).thenReturn(prepAnswerMock);

        final MigrateWithStorageAnswer migAnswerMock = mock(MigrateWithStorageAnswer.class);
        when(migAnswerMock.getResult()).thenReturn(true);
        when(_agentMgr.send(anyLong(), isA(MigrateWithStorageCommand.class))).thenReturn(migAnswerMock);

        final MigrateWithStorageReceiveAnswer migRecAnswerMock = mock(MigrateWithStorageReceiveAnswer.class);
        when(migRecAnswerMock.getResult()).thenReturn(true);
        when(_agentMgr.send(anyLong(), isA(MigrateWithStorageReceiveCommand.class))).thenReturn(migRecAnswerMock);

        final MigrateWithStorageSendAnswer migSendAnswerMock = mock(MigrateWithStorageSendAnswer.class);
        when(migSendAnswerMock.getResult()).thenReturn(true);
        when(_agentMgr.send(anyLong(), isA(MigrateWithStorageSendCommand.class))).thenReturn(migSendAnswerMock);

        final MigrateWithStorageCompleteAnswer migCompleteAnswerMock = mock(MigrateWithStorageCompleteAnswer.class);
        when(migCompleteAnswerMock.getResult()).thenReturn(true);
        when(_agentMgr.send(anyLong(), isA(MigrateWithStorageCompleteCommand.class))).thenReturn(migCompleteAnswerMock);

        final CheckVirtualMachineAnswer checkVmAnswerMock = mock(CheckVirtualMachineAnswer.class);
        when(checkVmAnswerMock.getResult()).thenReturn(true);
        when(checkVmAnswerMock.getState()).thenReturn(PowerState.PowerOn);
        when(_agentMgr.send(anyLong(), isA(CheckVirtualMachineCommand.class))).thenReturn(checkVmAnswerMock);

        // Mock the state transitions of vm.
        final Pair<Long, Long> opaqueMock = new Pair<>(_vmMock.getHostId(), _destHostMock.getId());
        when(_vmSnapshotMgr.hasActiveVMSnapshotTasks(anyLong())).thenReturn(false);
        when(_vmInstanceDao.updateState(State.Running, Event.MigrationRequested, State.Migrating, _vmMock, opaqueMock)).thenReturn(true);
        when(_vmInstanceDao.updateState(State.Migrating, Event.OperationSucceeded, State.Running, _vmMock, opaqueMock)).thenReturn(true);
    }

    // Check migration of a vm with its volumes across a cluster.
    @Test
    public void testMigrateWithVolumeAcrossCluster() throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException, OperationTimedoutException {

        initializeMockConfigForMigratingVmWithVolumes();
        when(_srcHostMock.getClusterId()).thenReturn(3L);
        when(_destHostMock.getClusterId()).thenReturn(4L);

        _vmMgr.migrateWithStorage(_vmInstance.getUuid(), _srcHostMock.getId(), _destHostMock.getId(), _volumeToPoolMock);
    }

    // Check migration of a vm fails when src and destination pool are not of same type; that is, one is shared and
    // other is local.
    @Test(expected = CloudRuntimeException.class)
    public void testMigrateWithVolumeFail1() throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException, OperationTimedoutException {

        initializeMockConfigForMigratingVmWithVolumes();
        when(_srcHostMock.getClusterId()).thenReturn(3L);
        when(_destHostMock.getClusterId()).thenReturn(3L);

        when(_destStoragePoolMock.isLocal()).thenReturn(true);
        when(_diskOfferingMock.getUseLocalStorage()).thenReturn(false);

        _vmMgr.migrateWithStorage(_vmInstance.getUuid(), _srcHostMock.getId(), _destHostMock.getId(), _volumeToPoolMock);
    }

    // Check migration of a vm fails when vm is not in Running state.
    @Test(expected = ConcurrentOperationException.class)
    public void testMigrateWithVolumeFail2() throws ResourceUnavailableException, ConcurrentOperationException, ManagementServerException,
            VirtualMachineMigrationException, OperationTimedoutException {

        initializeMockConfigForMigratingVmWithVolumes();
        when(_srcHostMock.getClusterId()).thenReturn(3L);
        when(_destHostMock.getClusterId()).thenReturn(3L);

        when(_vmMock.getState()).thenReturn(State.Stopped);

        _vmMgr.migrateWithStorage(_vmInstance.getUuid(), _srcHostMock.getId(), _destHostMock.getId(), _volumeToPoolMock);
    }

    @Test
    public void testSendStopWithOkAnswer() throws Exception {
        final VirtualMachineGuru guru = mock(VirtualMachineGuru.class);
        final VirtualMachine vm = mock(VirtualMachine.class);
        final VirtualMachineProfile profile = mock(VirtualMachineProfile.class);
        final StopAnswer answer = new StopAnswer(new StopCommand(vm, false, false), "ok", true);
        when(profile.getVirtualMachine()).thenReturn(vm);
        when(vm.getHostId()).thenReturn(1L);
        when(_agentMgr.send(anyLong(), (Command) any())).thenReturn(answer);

        final boolean actual = _vmMgr.sendStop(guru, profile, false, false);

        Assert.assertTrue(actual);
    }

    @Test
    public void testSendStopWithFailAnswer() throws Exception {
        final VirtualMachineGuru guru = mock(VirtualMachineGuru.class);
        final VirtualMachine vm = mock(VirtualMachine.class);
        final VirtualMachineProfile profile = mock(VirtualMachineProfile.class);
        final StopAnswer answer = new StopAnswer(new StopCommand(vm, false, false), "fail", false);
        when(profile.getVirtualMachine()).thenReturn(vm);
        when(vm.getHostId()).thenReturn(1L);
        when(_agentMgr.send(anyLong(), (Command) any())).thenReturn(answer);

        final boolean actual = _vmMgr.sendStop(guru, profile, false, false);

        Assert.assertFalse(actual);
    }

    @Test
    public void testSendStopWithNullAnswer() throws Exception {
        final VirtualMachineGuru guru = mock(VirtualMachineGuru.class);
        final VirtualMachine vm = mock(VirtualMachine.class);
        final VirtualMachineProfile profile = mock(VirtualMachineProfile.class);
        when(profile.getVirtualMachine()).thenReturn(vm);
        when(vm.getHostId()).thenReturn(1L);
        when(_agentMgr.send(anyLong(), (Command) any())).thenReturn(null);

        final boolean actual = _vmMgr.sendStop(guru, profile, false, false);

        Assert.assertFalse(actual);
    }

    @Test
    public void testExeceuteInSequence() {
        assertTrue(_vmMgr.getExecuteInSequence(HypervisorType.XenServer) == false);
        assertTrue(_vmMgr.getExecuteInSequence(HypervisorType.KVM) == false);
    }

    @Test
    public void testAllocate() throws Exception {
        final DeploymentPlan deploymentPlan = getDeploymentPlan();
        final ServiceOfferingVO computeOffering = getSvcoffering(512);
        final DiskOfferingInfo rootDiskOfferingInfo = getDiskOfferingInfo();

        _vmMgr.allocate("myVM", _templateMock, computeOffering, rootDiskOfferingInfo, null, null,
                deploymentPlan, HypervisorType.KVM, DiskControllerType.VIRTIO);
    }

    @Test
    public void testAllocateResetsCompliance() throws Exception {
        when(_vmMock.getComplianceStatus()).thenReturn(ComplianceStatus.VMNeedsRestart);

        testAllocate();

        verify(_vmMock).setComplianceStatus(ComplianceStatus.Compliant);
    }

    @Test
    public void testOrchestrateStart() throws Exception {
        final DeployDestination deployDestination = new DeployDestination(null, _pod, _cluster, _host);
        final VirtualMachineGuru guru = mock(VirtualMachineGuru.class);
        final DiskTO[] disks = {};
        when(guru.finalizeStart(any(VirtualMachineProfile.class), anyLong(), any(Commands.class), any(ReservationContext.class))).thenReturn(true);
        _vmMgr._vmGurus = new HashMap<>();
        _vmMgr._vmGurus.put(VirtualMachineType.User, guru);
        final HypervisorGuru guruMock = mock(HypervisorGuru.class);
        final VirtualMachineTO vmTO = mock(VirtualMachineTO.class);
        when(vmTO.getDisks()).thenReturn(disks);
        when(guruMock.implement(any(VirtualMachineProfile.class))).thenReturn(vmTO);
        when(_hvGuruMgr.getGuru(any(HypervisorType.class))).thenReturn(guruMock);

        when(_dpMgr.planDeployment(any(VirtualMachineProfile.class), any(DeploymentPlan.class), any(DeploymentPlanner.ExcludeList.class), any(DeploymentPlanner.class))).thenReturn(deployDestination);
        doReturn(true).when(this._vmMgr).stateTransitTo(eq(_vmMock), eq(Event.StartRequested), eq(null), any(String.class));
        doReturn(true).when(this._vmMgr).changeState(eq(_vmMock), eq(Event.OperationRetry), anyLong(), any(ItWorkVO.class), eq(ItWorkVO.Step.Prepare));
        doReturn(true).when(this._vmMgr).changeState(eq(_vmMock), eq(Event.OperationSucceeded), anyLong(), any(ItWorkVO.class), eq(ItWorkVO.Step.Done));

        when(_work.getStep()).thenReturn(ItWorkVO.Step.Prepare);

        when(_agentMgr.send(anyLong(), any(Commands.class))).thenAnswer(invocation -> {
            Commands cmds = ((Commands) invocation.getArguments()[1]);
            StartAnswer startAnswer = new StartAnswer(cmds.getCommand(StartCommand.class));
            com.cloud.legacymodel.communication.answer.Answer[] answers = {startAnswer};
            cmds.setAnswers(answers);
            return answers;
        });

        _vmMgr.orchestrateStart("uuid", null, null, null);
    }

    @Test
    public void testOrchestrateStartResetsCompliance() throws Exception {
        when(_vmMock.getComplianceStatus()).thenReturn(ComplianceStatus.VMNeedsRestart);

        testOrchestrateStart();

        verify(_vmMock).setComplianceStatus(ComplianceStatus.Compliant);
    }

    @After
    public void tearDown() {
        CallContext.unregister();
    }
}
