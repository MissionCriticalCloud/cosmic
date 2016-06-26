package src;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreateVMSnapshotAnswer;
import com.cloud.agent.api.DeleteVMSnapshotAnswer;
import com.cloud.agent.api.RevertToVMSnapshotAnswer;
import com.cloud.agent.api.VMSnapshotTO;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.GuestOSHypervisorVO;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.GuestOSHypervisorDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.snapshot.VMSnapshot;
import com.cloud.vm.snapshot.VMSnapshotVO;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;
import org.apache.cloudstack.engine.subsystem.api.storage.VMSnapshotStrategy;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.to.VolumeObjectTO;
import org.apache.cloudstack.storage.vmsnapshot.DefaultVMSnapshotStrategy;
import org.apache.cloudstack.storage.vmsnapshot.VMSnapshotHelper;
import org.apache.cloudstack.test.utils.SpringUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class VMSnapshotStrategyTest extends TestCase {
    @Inject
    VMSnapshotStrategy vmSnapshotStrategy;
    @Inject
    VMSnapshotHelper vmSnapshotHelper;
    @Inject
    UserVmDao userVmDao;
    @Inject
    GuestOSDao guestOSDao;
    @Inject
    GuestOSHypervisorDao guestOsHypervisorDao;
    @Inject
    AgentManager agentMgr;
    @Inject
    VMSnapshotDao vmSnapshotDao;
    @Inject
    HostDao hostDao;

    @Override
    @Before
    public void setUp() {
        ComponentContext.initComponentsLifeCycle();
    }

    @Test
    public void testCreateVMSnapshot() throws AgentUnavailableException, OperationTimedoutException {
        final Long hostId = 1L;
        final Long vmId = 1L;
        final Long guestOsId = 1L;
        final HypervisorType hypervisorType = HypervisorType.Any;
        final String hypervisorVersion = "default";
        final String guestOsName = "Other";
        final List<VolumeObjectTO> volumeObjectTOs = new ArrayList<>();
        final VMSnapshotVO vmSnapshot = Mockito.mock(VMSnapshotVO.class);
        final UserVmVO userVmVO = Mockito.mock(UserVmVO.class);
        Mockito.when(userVmVO.getGuestOSId()).thenReturn(guestOsId);
        Mockito.when(vmSnapshot.getVmId()).thenReturn(vmId);
        Mockito.when(vmSnapshotHelper.pickRunningHost(Matchers.anyLong())).thenReturn(hostId);
        Mockito.when(vmSnapshotHelper.getVolumeTOList(Matchers.anyLong())).thenReturn(volumeObjectTOs);
        Mockito.when(userVmDao.findById(Matchers.anyLong())).thenReturn(userVmVO);
        final GuestOSVO guestOSVO = Mockito.mock(GuestOSVO.class);
        Mockito.when(guestOSDao.findById(Matchers.anyLong())).thenReturn(guestOSVO);
        final GuestOSHypervisorVO guestOSHypervisorVO = Mockito.mock(GuestOSHypervisorVO.class);
        Mockito.when(guestOSHypervisorVO.getGuestOsName()).thenReturn(guestOsName);
        Mockito.when(guestOsHypervisorDao.findById(Matchers.anyLong())).thenReturn(guestOSHypervisorVO);
        Mockito.when(guestOsHypervisorDao.findByOsIdAndHypervisor(Matchers.anyLong(), Matchers.anyString(), Matchers.anyString())).thenReturn(guestOSHypervisorVO);
        Mockito.when(agentMgr.send(Matchers.anyLong(), Matchers.any(Command.class))).thenReturn(null);
        final HostVO hostVO = Mockito.mock(HostVO.class);
        Mockito.when(hostDao.findById(Matchers.anyLong())).thenReturn(hostVO);
        Mockito.when(hostVO.getHypervisorType()).thenReturn(hypervisorType);
        Mockito.when(hostVO.getHypervisorVersion()).thenReturn(hypervisorVersion);
        Exception e = null;
        try {
            vmSnapshotStrategy.takeVMSnapshot(vmSnapshot);
        } catch (final CloudRuntimeException e1) {
            e = e1;
        }

        assertNotNull(e);
        final CreateVMSnapshotAnswer answer = Mockito.mock(CreateVMSnapshotAnswer.class);
        Mockito.when(answer.getResult()).thenReturn(true);
        Mockito.when(agentMgr.send(Matchers.anyLong(), Matchers.any(Command.class))).thenReturn(answer);
        Mockito.when(vmSnapshotDao.findById(Matchers.anyLong())).thenReturn(vmSnapshot);
        VMSnapshot snapshot = null;
        snapshot = vmSnapshotStrategy.takeVMSnapshot(vmSnapshot);
        assertNotNull(snapshot);
    }

    @Test
    public void testRevertSnapshot() throws AgentUnavailableException, OperationTimedoutException {
        final Long hostId = 1L;
        final Long vmId = 1L;
        final Long guestOsId = 1L;
        final HypervisorType hypervisorType = HypervisorType.Any;
        final String hypervisorVersion = "default";
        final String guestOsName = "Other";
        final List<VolumeObjectTO> volumeObjectTOs = new ArrayList<>();
        final VMSnapshotVO vmSnapshot = Mockito.mock(VMSnapshotVO.class);
        final UserVmVO userVmVO = Mockito.mock(UserVmVO.class);
        Mockito.when(userVmVO.getGuestOSId()).thenReturn(guestOsId);
        Mockito.when(vmSnapshot.getVmId()).thenReturn(vmId);
        Mockito.when(vmSnapshotHelper.pickRunningHost(Matchers.anyLong())).thenReturn(hostId);
        Mockito.when(vmSnapshotHelper.getVolumeTOList(Matchers.anyLong())).thenReturn(volumeObjectTOs);
        Mockito.when(userVmDao.findById(Matchers.anyLong())).thenReturn(userVmVO);
        final GuestOSVO guestOSVO = Mockito.mock(GuestOSVO.class);
        Mockito.when(guestOSDao.findById(Matchers.anyLong())).thenReturn(guestOSVO);
        final GuestOSHypervisorVO guestOSHypervisorVO = Mockito.mock(GuestOSHypervisorVO.class);
        Mockito.when(guestOSHypervisorVO.getGuestOsName()).thenReturn(guestOsName);
        Mockito.when(guestOsHypervisorDao.findById(Matchers.anyLong())).thenReturn(guestOSHypervisorVO);
        Mockito.when(guestOsHypervisorDao.findByOsIdAndHypervisor(Matchers.anyLong(), Matchers.anyString(), Matchers.anyString())).thenReturn(guestOSHypervisorVO);
        final VMSnapshotTO vmSnapshotTO = Mockito.mock(VMSnapshotTO.class);
        Mockito.when(vmSnapshotHelper.getSnapshotWithParents(Matchers.any(VMSnapshotVO.class))).thenReturn(vmSnapshotTO);
        Mockito.when(vmSnapshotDao.findById(Matchers.anyLong())).thenReturn(vmSnapshot);
        Mockito.when(vmSnapshot.getId()).thenReturn(1L);
        Mockito.when(vmSnapshot.getCreated()).thenReturn(new Date());
        Mockito.when(agentMgr.send(Matchers.anyLong(), Matchers.any(Command.class))).thenReturn(null);
        final HostVO hostVO = Mockito.mock(HostVO.class);
        Mockito.when(hostDao.findById(Matchers.anyLong())).thenReturn(hostVO);
        Mockito.when(hostVO.getHypervisorType()).thenReturn(hypervisorType);
        Mockito.when(hostVO.getHypervisorVersion()).thenReturn(hypervisorVersion);
        Exception e = null;
        try {
            vmSnapshotStrategy.revertVMSnapshot(vmSnapshot);
        } catch (final CloudRuntimeException e1) {
            e = e1;
        }

        assertNotNull(e);

        final RevertToVMSnapshotAnswer answer = Mockito.mock(RevertToVMSnapshotAnswer.class);
        Mockito.when(answer.getResult()).thenReturn(Boolean.TRUE);
        Mockito.when(agentMgr.send(Matchers.anyLong(), Matchers.any(Command.class))).thenReturn(answer);
        final boolean result = vmSnapshotStrategy.revertVMSnapshot(vmSnapshot);
        assertTrue(result);
    }

    @Test
    public void testDeleteVMSnapshot() throws AgentUnavailableException, OperationTimedoutException {
        final Long hostId = 1L;
        final Long vmId = 1L;
        final Long guestOsId = 1L;
        final HypervisorType hypervisorType = HypervisorType.Any;
        final String hypervisorVersion = "default";
        final String guestOsName = "Other";
        final List<VolumeObjectTO> volumeObjectTOs = new ArrayList<>();
        final VMSnapshotVO vmSnapshot = Mockito.mock(VMSnapshotVO.class);
        final UserVmVO userVmVO = Mockito.mock(UserVmVO.class);
        Mockito.when(userVmVO.getGuestOSId()).thenReturn(guestOsId);
        Mockito.when(vmSnapshot.getVmId()).thenReturn(vmId);
        Mockito.when(vmSnapshotHelper.pickRunningHost(Matchers.anyLong())).thenReturn(hostId);
        Mockito.when(vmSnapshotHelper.getVolumeTOList(Matchers.anyLong())).thenReturn(volumeObjectTOs);
        Mockito.when(userVmDao.findById(Matchers.anyLong())).thenReturn(userVmVO);
        final GuestOSVO guestOSVO = Mockito.mock(GuestOSVO.class);
        Mockito.when(guestOSDao.findById(Matchers.anyLong())).thenReturn(guestOSVO);
        final GuestOSHypervisorVO guestOSHypervisorVO = Mockito.mock(GuestOSHypervisorVO.class);
        Mockito.when(guestOSHypervisorVO.getGuestOsName()).thenReturn(guestOsName);
        Mockito.when(guestOsHypervisorDao.findById(Matchers.anyLong())).thenReturn(guestOSHypervisorVO);
        Mockito.when(guestOsHypervisorDao.findByOsIdAndHypervisor(Matchers.anyLong(), Matchers.anyString(), Matchers.anyString())).thenReturn(guestOSHypervisorVO);
        final VMSnapshotTO vmSnapshotTO = Mockito.mock(VMSnapshotTO.class);
        Mockito.when(vmSnapshotHelper.getSnapshotWithParents(Matchers.any(VMSnapshotVO.class))).thenReturn(vmSnapshotTO);
        Mockito.when(vmSnapshotDao.findById(Matchers.anyLong())).thenReturn(vmSnapshot);
        Mockito.when(vmSnapshot.getId()).thenReturn(1L);
        Mockito.when(vmSnapshot.getCreated()).thenReturn(new Date());
        Mockito.when(agentMgr.send(Matchers.anyLong(), Matchers.any(Command.class))).thenReturn(null);
        final HostVO hostVO = Mockito.mock(HostVO.class);
        Mockito.when(hostDao.findById(Matchers.anyLong())).thenReturn(hostVO);
        Mockito.when(hostVO.getHypervisorType()).thenReturn(hypervisorType);
        Mockito.when(hostVO.getHypervisorVersion()).thenReturn(hypervisorVersion);

        Exception e = null;
        try {
            vmSnapshotStrategy.deleteVMSnapshot(vmSnapshot);
        } catch (final CloudRuntimeException e1) {
            e = e1;
        }

        assertNotNull(e);

        final DeleteVMSnapshotAnswer answer = Mockito.mock(DeleteVMSnapshotAnswer.class);
        Mockito.when(answer.getResult()).thenReturn(true);
        Mockito.when(agentMgr.send(Matchers.anyLong(), Matchers.any(Command.class))).thenReturn(answer);

        final boolean result = vmSnapshotStrategy.deleteVMSnapshot(vmSnapshot);
        assertTrue(result);
    }

    @Configuration
    @ComponentScan(basePackageClasses = {NetUtils.class, DefaultVMSnapshotStrategy.class},
            includeFilters = {@ComponentScan.Filter(value = TestConfiguration.Library.class, type = FilterType.CUSTOM)},
            useDefaultFilters = false)
    public static class TestConfiguration extends SpringUtils.CloudStackTestConfiguration {

        @Bean
        public VMSnapshotHelper vmSnapshotHelper() {
            return Mockito.mock(VMSnapshotHelper.class);
        }

        @Bean
        public GuestOSDao guestOSDao() {
            return Mockito.mock(GuestOSDao.class);
        }

        @Bean
        public GuestOSHypervisorDao guestOsHypervisorDao() {
            return Mockito.mock(GuestOSHypervisorDao.class);
        }

        @Bean
        public UserVmDao userVmDao() {
            return Mockito.mock(UserVmDao.class);
        }

        @Bean
        public VMSnapshotDao vmSnapshotDao() {
            return Mockito.mock(VMSnapshotDao.class);
        }

        @Bean
        public ConfigurationDao configurationDao() {
            return Mockito.mock(ConfigurationDao.class);
        }

        @Bean
        public AgentManager agentManager() {
            return Mockito.mock(AgentManager.class);
        }

        @Bean
        public VolumeDao volumeDao() {
            return Mockito.mock(VolumeDao.class);
        }

        @Bean
        public DiskOfferingDao diskOfferingDao() {
            return Mockito.mock(DiskOfferingDao.class);
        }

        @Bean
        public HostDao hostDao() {
            return Mockito.mock(HostDao.class);
        }

        public static class Library implements TypeFilter {
            @Override
            public boolean match(final MetadataReader mdr, final MetadataReaderFactory arg1) throws IOException {
                mdr.getClassMetadata().getClassName();
                final ComponentScan cs = TestConfiguration.class.getAnnotation(ComponentScan.class);
                return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
            }
        }
    }
}
