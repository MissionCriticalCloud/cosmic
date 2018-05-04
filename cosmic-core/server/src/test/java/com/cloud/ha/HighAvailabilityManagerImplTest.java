package com.cloud.ha;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.AgentManager;
import com.cloud.alert.AlertManager;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.engine.orchestration.service.VolumeOrchestrationService;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.ha.HaWork.HaWorkStep;
import com.cloud.ha.HaWork.HaWorkType;
import com.cloud.ha.dao.HighAvailabilityDao;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.managed.context.ManagedContext;
import com.cloud.model.enumeration.HostType;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.resource.ResourceManager;
import com.cloud.server.ManagementServer;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.StorageManager;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.user.AccountManager;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.VMInstanceDao;

import javax.inject.Inject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class HighAvailabilityManagerImplTest {
    private static final Logger s_logger = LoggerFactory.getLogger(HighAvailabilityManagerImplTest.class);
    static Method processWorkMethod = null;
    @Mock
    HighAvailabilityDao _haDao;
    @Mock
    VMInstanceDao _instanceDao;
    @Mock
    HostDao _hostDao;
    @Mock
    HostPodDao _podDao;
    @Mock
    ClusterDetailsDao _clusterDetailsDao;
    @Mock
    ServiceOfferingDao _serviceOfferingDao;
    @Mock
    ManagedContext _managedContext;
    @Mock
    AgentManager _agentMgr;
    @Mock
    AlertManager _alertMgr;
    @Mock
    StorageManager _storageMgr;
    @Mock
    GuestOSDao _guestOSDao;
    @Mock
    GuestOSCategoryDao _guestOSCategoryDao;
    @Mock
    VirtualMachineManager _itMgr;
    @Mock
    AccountManager _accountMgr;
    @Mock
    ResourceManager _resourceMgr;
    @Mock
    ManagementServer _msServer;
    @Mock
    ConfigurationDao _configDao;
    @Mock
    VolumeOrchestrationService volumeMgr;
    @Mock
    HostVO hostVO;
    @Mock
    ZoneRepository zoneRepository;
    HighAvailabilityManagerImpl highAvailabilityManager;
    HighAvailabilityManagerImpl highAvailabilityManagerSpy;

    @BeforeClass
    public static void initOnce() {
        try {
            processWorkMethod = HighAvailabilityManagerImpl.class.getDeclaredMethod("processWork", HaWorkVO.class);
            processWorkMethod.setAccessible(true);
        } catch (final NoSuchMethodException e) {
            s_logger.info("[ignored] expected NoSuchMethodException caught: " + e.getLocalizedMessage());
        }
    }

    @Before
    public void setup() throws IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException {
        highAvailabilityManager = new HighAvailabilityManagerImpl();
        for (final Field injectField : HighAvailabilityManagerImpl.class.getDeclaredFields()) {
            if (injectField.isAnnotationPresent(Inject.class)) {
                injectField.setAccessible(true);
                injectField.set(highAvailabilityManager, this.getClass().getDeclaredField(injectField.getName()).get(this));
            } else if (injectField.getName().equals("_workers")) {
                injectField.setAccessible(true);
                for (final Class<?> clz : HighAvailabilityManagerImpl.class.getDeclaredClasses()) {
                    if (clz.getName().equals("com.cloud.ha.HighAvailabilityManagerImpl$WorkerThread")) {
                        final Object obj = Array.newInstance(clz, 0);
                        injectField.set(highAvailabilityManager, obj);
                    }
                }
            } else if (injectField.getName().equals("_maxRetries")) {
                injectField.setAccessible(true);
                injectField.set(highAvailabilityManager, 5);
            }
        }
        highAvailabilityManagerSpy = Mockito.spy(highAvailabilityManager);
    }

    @Test
    public void scheduleRestartForVmsOnHost() {
        Mockito.when(hostVO.getType()).thenReturn(HostType.Routing);
        Mockito.when(hostVO.getHypervisorType()).thenReturn(HypervisorType.KVM);
        Mockito.when(_instanceDao.listByHostId(42l)).thenReturn(Arrays.asList(Mockito.mock(VMInstanceVO.class)));
        Mockito.when(_podDao.findById(Mockito.anyLong())).thenReturn(Mockito.mock(HostPodVO.class));
        Mockito.when(zoneRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(Mockito.mock(Zone.class)));

        highAvailabilityManager.scheduleRestartForVmsOnHost(hostVO, true);
    }

    @Test
    public void scheduleRestartForVmsOnHostNonEmptyVMList() {
        Mockito.when(hostVO.getId()).thenReturn(1l);
        Mockito.when(hostVO.getType()).thenReturn(HostType.Routing);
        Mockito.when(hostVO.getHypervisorType()).thenReturn(HypervisorType.XenServer);
        final List<VMInstanceVO> vms = new ArrayList<>();
        final VMInstanceVO vm1 = Mockito.mock(VMInstanceVO.class);
        Mockito.when(vm1.getHostId()).thenReturn(1l);
        Mockito.when(vm1.getInstanceName()).thenReturn("i-2-3-VM");
        Mockito.when(vm1.getType()).thenReturn(VirtualMachineType.User);
        Mockito.when(vm1.isHaEnabled()).thenReturn(true);
        vms.add(vm1);
        final VMInstanceVO vm2 = Mockito.mock(VMInstanceVO.class);
        Mockito.when(vm2.getHostId()).thenReturn(1l);
        Mockito.when(vm2.getInstanceName()).thenReturn("r-2-VM");
        Mockito.when(vm2.getType()).thenReturn(VirtualMachineType.DomainRouter);
        Mockito.when(vm2.isHaEnabled()).thenReturn(true);
        vms.add(vm2);
        Mockito.when(_instanceDao.listByHostId(Mockito.anyLong())).thenReturn(vms);
        Mockito.when(_instanceDao.findByUuid(vm1.getUuid())).thenReturn(vm1);
        Mockito.when(_instanceDao.findByUuid(vm2.getUuid())).thenReturn(vm2);
        Mockito.when(_podDao.findById(Mockito.anyLong())).thenReturn(Mockito.mock(HostPodVO.class));
        Mockito.when(zoneRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(Mockito.mock(Zone.class)));
        Mockito.when(_haDao.findPreviousHA(Mockito.anyLong())).thenReturn(Arrays.asList(Mockito.mock(HaWorkVO.class)));
        Mockito.when(_haDao.persist((HaWorkVO) Mockito.anyObject())).thenReturn(Mockito.mock(HaWorkVO.class));

        highAvailabilityManager.scheduleRestartForVmsOnHost(hostVO, true);
    }

    @Test
    public void investigateHostStatusSuccess() {
        Mockito.when(_hostDao.findById(Mockito.anyLong())).thenReturn(hostVO);
        // Set the list of investigators, CheckOnAgentInvestigator suffices for now
        final Investigator investigator = Mockito.mock(CheckOnAgentInvestigator.class);
        final List<Investigator> investigators = new ArrayList<>();
        investigators.add(investigator);
        highAvailabilityManager.setInvestigators(investigators);
        // Mock isAgentAlive to return host status as Down
        Mockito.when(investigator.isAgentAlive(hostVO)).thenReturn(HostStatus.Down);

        assertTrue(highAvailabilityManager.investigate(1l) == HostStatus.Down);
    }

    @Test
    public void investigateHostStatusFailure() {
        Mockito.when(_hostDao.findById(Mockito.anyLong())).thenReturn(hostVO);
        // Set the list of investigators, CheckOnAgentInvestigator suffices for now
        // Also no need to mock isAgentAlive() as actual implementation returns null
        final Investigator investigator = Mockito.mock(CheckOnAgentInvestigator.class);
        final List<Investigator> investigators = new ArrayList<>();
        investigators.add(investigator);
        highAvailabilityManager.setInvestigators(investigators);

        assertNull(highAvailabilityManager.investigate(1l));
    }

    @Test
    public void processWorkWithRetryCountExceeded() {
        processWorkWithRetryCount(5, HaWorkStep.Done); // max retry count is 5
    }

    private void processWorkWithRetryCount(final int count, final HaWorkStep expectedStep) {
        assertNotNull(processWorkMethod);
        final HaWorkVO work = new HaWorkVO(1l, VirtualMachineType.User, HaWorkType.Migration, HaWork.HaWorkStep.Scheduled, 1l, VirtualMachine.State.Running, count, 12345678l);
        Mockito.doReturn(12345678l).when(highAvailabilityManagerSpy).migrate(work);
        try {
            processWorkMethod.invoke(highAvailabilityManagerSpy, work);
        } catch (final IllegalAccessException e) {
            s_logger.info("[ignored] expected IllegalAccessException caught: " + e.getLocalizedMessage());
        } catch (final IllegalArgumentException e) {
            s_logger.info("[ignored] expected IllegalArgumentException caught: " + e.getLocalizedMessage());
        } catch (final InvocationTargetException e) {
            s_logger.info("[ignored] expected InvocationTargetException caught: " + e.getLocalizedMessage());
        }
        assertTrue(work.getStep() == expectedStep);
    }

    @Test
    public void processWorkWithRetryCountNotExceeded() {
        processWorkWithRetryCount(3, HaWork.HaWorkStep.Scheduled);
    }
}
