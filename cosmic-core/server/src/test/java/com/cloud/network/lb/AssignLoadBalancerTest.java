package com.cloud.network.lb;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.NetworkModelImpl;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVMMapDao;
import com.cloud.network.dao.LoadBalancerVMMapVO;
import com.cloud.network.dao.LoadBalancerVO;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.RulesManagerImpl;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.User;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.uservm.UserVm;
import com.cloud.vm.Nic;
import com.cloud.vm.NicVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.dao.NicSecondaryIpDao;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.command.user.loadbalancer.AssignToLoadBalancerRuleCmd;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.context.CallContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

public class AssignLoadBalancerTest {

    private static final long domainId = 5L;
    private static final long accountId = 5L;
    private static final String accountName = "admin";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Inject
    AccountManager _accountMgr;
    @Inject
    AccountManager _acctMgr;
    @Inject
    AccountDao _accountDao;
    @Inject
    DomainDao _domainDao;
    @Mock
    List<LoadBalancerVMMapVO> _lbvmMapList;
    @Mock
    List<Nic> nic;
    @Mock
    UserVmDao userDao;
    @Spy
    RulesManagerImpl _rulesMgr = new RulesManagerImpl() {
        @Override
        public void checkRuleAndUserVm(final FirewallRule rule, final UserVm userVm, final Account caller) {

        }
    };
    @Spy
    NicVO nicvo = new NicVO() {

    };
    @Spy
    NetworkModelImpl _networkModel = new NetworkModelImpl() {
        @Override
        public List<? extends Nic> getNics(final long vmId) {
            nic = new ArrayList<>();
            nicvo.setNetworkId(204L);
            nic.add(nicvo);
            return nic;
        }
    };
    LoadBalancingRulesManagerImpl _lbMgr = new LoadBalancingRulesManagerImpl();
    private AssignToLoadBalancerRuleCmd assignToLbRuleCmd;
    private ResponseGenerator responseGenerator;
    private SuccessResponse successResponseGenerator;

    @Before
    public void setUp() {
        assignToLbRuleCmd = new AssignToLoadBalancerRuleCmd() {
        };

        // ComponentContext.initComponentsLifeCycle();
        final AccountVO account = new AccountVO(accountName, domainId, "networkDomain", Account.ACCOUNT_TYPE_NORMAL, "uuid");
        final DomainVO domain = new DomainVO("rootDomain", 5L, 5L, "networkDomain");

        final UserVO user = new UserVO(1, "testuser", "password", "firstname", "lastName", "email", "timezone", UUID.randomUUID().toString(), User.Source.UNKNOWN);

        CallContext.register(user, account);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testBothArgsEmpty() throws ResourceAllocationException, ResourceUnavailableException, InsufficientCapacityException {

        final Map<Long, List<String>> emptyMap = new HashMap<>();

        final LoadBalancerDao lbdao = Mockito.mock(LoadBalancerDao.class);
        _lbMgr._lbDao = lbdao;

        when(lbdao.findById(anyLong())).thenReturn(Mockito.mock(LoadBalancerVO.class));

        _lbMgr.assignToLoadBalancer(1L, null, emptyMap);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testNicIsNotInNw() throws ResourceAllocationException, ResourceUnavailableException, InsufficientCapacityException {

        final Map<Long, List<String>> vmIdIpMap = new HashMap<>();
        final List<String> secIp = new ArrayList<>();
        secIp.add("10.1.1.175");
        vmIdIpMap.put(1L, secIp);

        final List<Long> vmIds = new ArrayList<>();
        vmIds.add(2L);

        final LoadBalancerDao lbDao = Mockito.mock(LoadBalancerDao.class);
        final LoadBalancerVMMapDao lb2VmMapDao = Mockito.mock(LoadBalancerVMMapDao.class);
        final UserVmDao userVmDao = Mockito.mock(UserVmDao.class);

        _lbMgr._lbDao = lbDao;
        _lbMgr._lb2VmMapDao = lb2VmMapDao;
        _lbMgr._vmDao = userVmDao;
        _lbvmMapList = new ArrayList<>();
        _lbMgr._rulesMgr = _rulesMgr;
        _lbMgr._networkModel = _networkModel;

        when(lbDao.findById(anyLong())).thenReturn(Mockito.mock(LoadBalancerVO.class));
        when(userVmDao.findById(anyLong())).thenReturn(Mockito.mock(UserVmVO.class));
        when(lb2VmMapDao.listByLoadBalancerId(anyLong(), anyBoolean())).thenReturn(_lbvmMapList);

        _lbMgr.assignToLoadBalancer(1L, null, vmIdIpMap);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void tesSecIpNotSetToVm() throws ResourceAllocationException, ResourceUnavailableException, InsufficientCapacityException {

        final AssignToLoadBalancerRuleCmd assignLbRuleCmd = Mockito.mock(AssignToLoadBalancerRuleCmd.class);

        final Map<Long, List<String>> vmIdIpMap = new HashMap<>();
        final List<String> secIp = new ArrayList<>();
        secIp.add("10.1.1.175");
        vmIdIpMap.put(1L, secIp);

        final List<Long> vmIds = new ArrayList<>();
        vmIds.add(2L);

        final LoadBalancerVO lbVO = new LoadBalancerVO("1", "L1", "Lbrule", 1, 22, 22, "rb", 204, 0, 0, "tcp");

        final LoadBalancerDao lbDao = Mockito.mock(LoadBalancerDao.class);
        final LoadBalancerVMMapDao lb2VmMapDao = Mockito.mock(LoadBalancerVMMapDao.class);
        final UserVmDao userVmDao = Mockito.mock(UserVmDao.class);
        final NicSecondaryIpDao nicSecIpDao = Mockito.mock(NicSecondaryIpDao.class);

        _lbMgr._lbDao = lbDao;
        _lbMgr._lb2VmMapDao = lb2VmMapDao;
        _lbMgr._vmDao = userVmDao;
        _lbMgr._nicSecondaryIpDao = nicSecIpDao;
        _lbvmMapList = new ArrayList<>();
        _lbMgr._rulesMgr = _rulesMgr;
        _lbMgr._networkModel = _networkModel;

        when(lbDao.findById(anyLong())).thenReturn(lbVO);
        when(userVmDao.findById(anyLong())).thenReturn(Mockito.mock(UserVmVO.class));
        when(lb2VmMapDao.listByLoadBalancerId(anyLong(), anyBoolean())).thenReturn(_lbvmMapList);
        when(nicSecIpDao.findByIp4AddressAndNicId(anyString(), anyLong())).thenReturn(null);

        _lbMgr.assignToLoadBalancer(1L, null, vmIdIpMap);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testVmIdAlreadyExist() throws ResourceAllocationException, ResourceUnavailableException, InsufficientCapacityException {

        final AssignToLoadBalancerRuleCmd assignLbRuleCmd = Mockito.mock(AssignToLoadBalancerRuleCmd.class);

        final Map<Long, List<String>> vmIdIpMap = new HashMap<>();
        final List<String> secIp = new ArrayList<>();
        secIp.add("10.1.1.175");
        vmIdIpMap.put(1L, secIp);

        final List<Long> vmIds = new ArrayList<>();
        vmIds.add(2L);

        final LoadBalancerVO lbVO = new LoadBalancerVO("1", "L1", "Lbrule", 1, 22, 22, "rb", 204, 0, 0, "tcp");

        final LoadBalancerDao lbDao = Mockito.mock(LoadBalancerDao.class);
        final LoadBalancerVMMapDao lb2VmMapDao = Mockito.mock(LoadBalancerVMMapDao.class);
        final UserVmDao userVmDao = Mockito.mock(UserVmDao.class);
        final NicSecondaryIpDao nicSecIpDao = Mockito.mock(NicSecondaryIpDao.class);
        final LoadBalancerVMMapVO lbVmMapVO = new LoadBalancerVMMapVO(1L, 1L, "10.1.1.175", false);

        _lbMgr._lbDao = lbDao;
        _lbMgr._lb2VmMapDao = lb2VmMapDao;
        _lbMgr._vmDao = userVmDao;
        _lbMgr._nicSecondaryIpDao = nicSecIpDao;
        _lbvmMapList = new ArrayList<>();
        _lbvmMapList.add(lbVmMapVO);
        _lbMgr._rulesMgr = _rulesMgr;
        _lbMgr._networkModel = _networkModel;

        when(lbDao.findById(anyLong())).thenReturn(lbVO);
        when(userVmDao.findById(anyLong())).thenReturn(Mockito.mock(UserVmVO.class));
        when(lb2VmMapDao.listByLoadBalancerId(anyLong(), anyBoolean())).thenReturn(_lbvmMapList);
        when(nicSecIpDao.findByIp4AddressAndNicId(anyString(), anyLong())).thenReturn(null);

        _lbMgr.assignToLoadBalancer(1L, null, vmIdIpMap);
    }

    @After
    public void tearDown() {
        CallContext.unregister();
    }
}
