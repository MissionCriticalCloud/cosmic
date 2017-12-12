package com.cloud.affinity;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.cloud.acl.ControlledEntity;
import com.cloud.affinity.dao.AffinityGroupDao;
import com.cloud.affinity.dao.AffinityGroupDomainMapDao;
import com.cloud.affinity.dao.AffinityGroupVMMapDao;
import com.cloud.api.command.user.affinitygroup.CreateAffinityGroupCmd;
import com.cloud.context.CallContext;
import com.cloud.dao.EntityManager;
import com.cloud.dc.dao.DedicatedResourceDao;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEventUtils;
import com.cloud.event.EventVO;
import com.cloud.event.dao.EventDao;
import com.cloud.exception.ResourceInUseException;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.framework.messagebus.MessageBus;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.projects.dao.ProjectDao;
import com.cloud.test.utils.SpringUtils;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountService;
import com.cloud.user.AccountVO;
import com.cloud.user.DomainManager;
import com.cloud.user.User;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
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
public class AffinityGroupServiceImplTest {

    private static final long DOMAIN_ID = 5L;
    private static final long PROJECT_ID = 10L;
    private static final String ACCOUNT_NAME = "user";
    private static final String AFFINITY_GROUP_NAME = "group1";
    @Inject
    AffinityGroupServiceImpl _affinityService;
    @Inject
    AccountManager _acctMgr;
    @Inject
    AffinityGroupProcessor _processor;
    @Inject
    AffinityGroupDao _groupDao;
    @Inject
    UserVmDao _vmDao;
    @Inject
    AffinityGroupVMMapDao _affinityGroupVMMapDao;
    @Inject
    AffinityGroupDao _affinityGroupDao;
    @Inject
    ActionEventUtils _eventUtils;
    @Inject
    AccountDao _accountDao;
    @Inject
    ProjectDao _projectDao;
    @Inject
    EventDao _eventDao;
    @Inject
    DedicatedResourceDao _dedicatedDao;
    private AccountVO acct;

    @BeforeClass
    public static void setUpClass() throws ConfigurationException {
    }

    @Before
    public void setUp() {
        ComponentContext.initComponentsLifeCycle();
        acct = new AccountVO(200L);
        acct.setType(Account.ACCOUNT_TYPE_NORMAL);
        acct.setAccountName(ACCOUNT_NAME);
        acct.setDomainId(DOMAIN_ID);

        final UserVO user = new UserVO(1, "testuser", "password", "firstname", "lastName", "email", "timezone", UUID.randomUUID().toString(), User.Source.UNKNOWN);

        CallContext.register(user, acct);

        when(_processor.getType()).thenReturn("mock");
        when(_accountDao.findByIdIncludingRemoved(0L)).thenReturn(acct);

        final List<AffinityGroupProcessor> affinityProcessors = new ArrayList<>();
        affinityProcessors.add(_processor);
        _affinityService.setAffinityGroupProcessors(affinityProcessors);

        final AffinityGroupVO group = new AffinityGroupVO(AFFINITY_GROUP_NAME, "mock", "mock group", DOMAIN_ID, 200L, ControlledEntity.ACLType.Account);
        Mockito.when(_affinityGroupDao.persist(Matchers.any(AffinityGroupVO.class))).thenReturn(group);
        Mockito.when(_affinityGroupDao.findById(Matchers.anyLong())).thenReturn(group);
        Mockito.when(_affinityGroupDao.findByAccountAndName(Matchers.anyLong(), Matchers.anyString())).thenReturn(group);
        Mockito.when(_affinityGroupDao.lockRow(Matchers.anyLong(), anyBoolean())).thenReturn(group);
        Mockito.when(_affinityGroupDao.expunge(Matchers.anyLong())).thenReturn(true);
        Mockito.when(_eventDao.persist(Matchers.any(EventVO.class))).thenReturn(new EventVO());
    }

    @After
    public void tearDown() {
        CallContext.unregister();
    }

    @Test
    public void createAffinityGroupFromCmdTest() {
        when(_acctMgr.finalizeOwner((Account) anyObject(), anyString(), anyLong(), anyLong())).thenReturn(acct);
        when(_groupDao.isNameInUse(anyLong(), anyLong(), eq(AFFINITY_GROUP_NAME))).thenReturn(false);
        final CreateAffinityGroupCmd mockCreateAffinityGroupCmd = Mockito.mock(CreateAffinityGroupCmd.class);
        when(mockCreateAffinityGroupCmd.getProjectId()).thenReturn(PROJECT_ID);
        when(mockCreateAffinityGroupCmd.getAffinityGroupName()).thenReturn(AFFINITY_GROUP_NAME);
        when(mockCreateAffinityGroupCmd.getAffinityGroupType()).thenReturn("mock");
        when(mockCreateAffinityGroupCmd.getDescription()).thenReturn("affinity group one");
        final AffinityGroup group = _affinityService.createAffinityGroup(mockCreateAffinityGroupCmd);
        assertNotNull("Affinity group 'group1' of type 'mock' failed to create ", group);
    }

    @Test
    public void createAffinityGroupTest() {
        when(_acctMgr.finalizeOwner((Account) anyObject(), anyString(), anyLong(), anyLong())).thenReturn(acct);
        when(_groupDao.isNameInUse(anyLong(), anyLong(), eq(AFFINITY_GROUP_NAME))).thenReturn(false);
        final AffinityGroup group = _affinityService.createAffinityGroup(ACCOUNT_NAME, null, DOMAIN_ID, AFFINITY_GROUP_NAME, "mock", "affinity group one");
        assertNotNull("Affinity group 'group1' of type 'mock' failed to create ", group);
    }

    @Test
    public void shouldDeleteDomainLevelAffinityGroup() {
        final AffinityGroupVO mockGroup = Mockito.mock(AffinityGroupVO.class);
        when(mockGroup.getId()).thenReturn(2L);
        when(_affinityGroupDao.findById(Matchers.anyLong())).thenReturn(mockGroup);
        _affinityService.deleteAffinityGroup(2L, null, null, DOMAIN_ID, null);
        Mockito.verify(_affinityGroupDao).expunge(2L);
    }

    @Test
    public void shouldDeleteAffintyGroupById() {
        final AffinityGroupVO mockGroup = Mockito.mock(AffinityGroupVO.class);
        when(mockGroup.getId()).thenReturn(1L);
        when(_affinityGroupDao.findById(Matchers.anyLong())).thenReturn(mockGroup);
        _affinityService.deleteAffinityGroup(1L, ACCOUNT_NAME, null, DOMAIN_ID, null);
        Mockito.verify(_affinityGroupDao).expunge(1L);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void invalidAffinityTypeTest() {
        when(_acctMgr.finalizeOwner((Account) anyObject(), anyString(), anyLong(), anyLong())).thenReturn(acct);
        _affinityService.createAffinityGroup(ACCOUNT_NAME, null, DOMAIN_ID, AFFINITY_GROUP_NAME, "invalid", "affinity group one");
    }

    @Test(expected = InvalidParameterValueException.class)
    public void uniqueAffinityNameTest() {
        when(_acctMgr.finalizeOwner((Account) anyObject(), anyString(), anyLong(), anyLong())).thenReturn(acct);
        when(_groupDao.isNameInUse(anyLong(), anyLong(), eq(AFFINITY_GROUP_NAME))).thenReturn(true);
        _affinityService.createAffinityGroup(ACCOUNT_NAME, null, DOMAIN_ID, AFFINITY_GROUP_NAME, "mock", "affinity group two");
    }

    @Test(expected = InvalidParameterValueException.class)
    public void deleteAffinityGroupInvalidIdTest() throws ResourceInUseException {
        when(_acctMgr.finalizeOwner((Account) anyObject(), anyString(), anyLong(), anyLong())).thenReturn(acct);
        when(_groupDao.findById(20L)).thenReturn(null);
        _affinityService.deleteAffinityGroup(20L, ACCOUNT_NAME, null, DOMAIN_ID, AFFINITY_GROUP_NAME);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void deleteAffinityGroupInvalidIdName() throws ResourceInUseException {
        when(_acctMgr.finalizeOwner((Account) anyObject(), anyString(), anyLong(), anyLong())).thenReturn(acct);
        when(_acctMgr.finalyzeAccountId(ACCOUNT_NAME, DOMAIN_ID, null, true)).thenReturn(200L);
        when(_groupDao.findByAccountAndName(200L, AFFINITY_GROUP_NAME)).thenReturn(null);
        _affinityService.deleteAffinityGroup(null, ACCOUNT_NAME, null, DOMAIN_ID, AFFINITY_GROUP_NAME);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void deleteAffinityGroupNullIdName() throws ResourceInUseException {
        when(_acctMgr.finalizeOwner((Account) anyObject(), anyString(), anyLong(), anyLong())).thenReturn(acct);
        _affinityService.deleteAffinityGroup(null, ACCOUNT_NAME, null, DOMAIN_ID, null);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void updateAffinityGroupVMRunning() throws ResourceInUseException {
        when(_acctMgr.finalizeOwner((Account) anyObject(), anyString(), anyLong(), anyLong())).thenReturn(acct);
        final UserVmVO vm = new UserVmVO(10L, "test", "test", 101L, HypervisorType.Any, 21L, false, false,
                DOMAIN_ID, 200L, 1, 5L, "", "test", 1L, null, null);
        vm.setState(VirtualMachine.State.Running);
        when(_vmDao.findById(10L)).thenReturn(vm);

        final List<Long> affinityGroupIds = new ArrayList<>();
        affinityGroupIds.add(20L);

        _affinityService.updateVMAffinityGroups(10L, affinityGroupIds);
    }

    @Configuration
    @ComponentScan(basePackageClasses = {AffinityGroupServiceImpl.class, ActionEventUtils.class}, includeFilters = {@Filter(value = TestConfiguration.Library.class, type =
            FilterType.CUSTOM)}, useDefaultFilters = false)
    public static class TestConfiguration extends SpringUtils.CloudStackTestConfiguration {

        @Bean
        public AccountDao accountDao() {
            return Mockito.mock(AccountDao.class);
        }

        @Bean
        public ProjectDao projectDao() {
            return Mockito.mock(ProjectDao.class);
        }

        @Bean
        public AccountService accountService() {
            return Mockito.mock(AccountService.class);
        }

        @Bean
        public AffinityGroupProcessor affinityGroupProcessor() {
            return Mockito.mock(AffinityGroupProcessor.class);
        }

        @Bean
        public AffinityGroupDao affinityGroupDao() {
            return Mockito.mock(AffinityGroupDao.class);
        }

        @Bean
        public AffinityGroupVMMapDao affinityGroupVMMapDao() {
            return Mockito.mock(AffinityGroupVMMapDao.class);
        }

        @Bean
        public DedicatedResourceDao dedicatedResourceDao() {
            return Mockito.mock(DedicatedResourceDao.class);
        }

        @Bean
        public AccountManager accountManager() {
            return Mockito.mock(AccountManager.class);
        }

        @Bean
        public DomainManager domainManager() {
            return Mockito.mock(DomainManager.class);
        }

        @Bean
        public EventDao eventDao() {
            return Mockito.mock(EventDao.class);
        }

        @Bean
        public UserVmDao userVMDao() {
            return Mockito.mock(UserVmDao.class);
        }

        @Bean
        public UserDao userDao() {
            return Mockito.mock(UserDao.class);
        }

        @Bean
        public AffinityGroupDomainMapDao affinityGroupDomainMapDao() {
            return Mockito.mock(AffinityGroupDomainMapDao.class);
        }

        @Bean
        public EntityManager entityManager() {
            return Mockito.mock(EntityManager.class);
        }

        @Bean
        public DomainDao domainDao() {
            return Mockito.mock(DomainDao.class);
        }

        @Bean
        public MessageBus messageBus() {
            return Mockito.mock(MessageBus.class);
        }

        @Bean
        public ConfigurationDao configDao() {
            return Mockito.mock(ConfigurationDao.class);
        }

        public static class Library implements TypeFilter {

            @Override
            public boolean match(final MetadataReader mdr, final MetadataReaderFactory arg1) throws IOException {
                final ComponentScan cs = TestConfiguration.class.getAnnotation(ComponentScan.class);
                return SpringUtils.includedInBasePackageClasses(mdr.getClassMetadata().getClassName(), cs);
            }
        }
    }
}
