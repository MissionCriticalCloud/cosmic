package com.cloud.network;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.VlanDao;
import com.cloud.lb.dao.ApplicationLoadBalancerRuleDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkVO;
import com.cloud.user.Account;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.net.Ip;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.cloud.vm.NicSecondaryIp;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.NicSecondaryIpDao;
import junit.framework.Assert;
import org.apache.bcel.generic.NEW;
import org.junit.Before;
import org.junit.Test;

public class NetworkModelTest {
    NetworkModelImpl modelImpl;
    @Before
    public void setUp() {
        modelImpl = new NetworkModelImpl();

    }

    @Test
    public void testGetSourceNatIpAddressForGuestNetwork() {
        final IPAddressDao ipAddressDao = mock(IPAddressDao.class);
        modelImpl._ipAddressDao = ipAddressDao;
        final List<IPAddressVO> fakeList = new ArrayList<>();
        final IPAddressVO fakeIp = new IPAddressVO(new Ip("75.75.75.75"), 1, 0xaabbccddeeffL, 10, false);
        fakeList.add(fakeIp);
        final SearchBuilder<IPAddressVO> fakeSearch = mock(SearchBuilder.class);
        modelImpl.IpAddressSearch = fakeSearch;
        final VlanDao fakeVlanDao = mock(VlanDao.class);
        when(fakeVlanDao.findById(anyLong())).thenReturn(mock(VlanVO.class));
        modelImpl._vlanDao = fakeVlanDao;
        when(fakeSearch.create()).thenReturn(mock(SearchCriteria.class));
        when(ipAddressDao.search(any(SearchCriteria.class), (Filter) org.mockito.Matchers.isNull())).thenReturn(fakeList);
        when(ipAddressDao.findById(anyLong())).thenReturn(fakeIp);
        final Account fakeAccount = mock(Account.class);
        when(fakeAccount.getId()).thenReturn(1L);
        final Network fakeNetwork = mock(Network.class);
        when(fakeNetwork.getId()).thenReturn(1L);
        PublicIpAddress answer = modelImpl.getSourceNatIpAddressForGuestNetwork(fakeAccount, fakeNetwork);
        Assert.assertNull(answer);
        final IPAddressVO fakeIp2 = new IPAddressVO(new Ip("76.75.75.75"), 1, 0xaabb10ddeeffL, 10, true);
        fakeList.add(fakeIp2);
        when(ipAddressDao.findById(anyLong())).thenReturn(fakeIp2);
        answer = modelImpl.getSourceNatIpAddressForGuestNetwork(fakeAccount, fakeNetwork);
        Assert.assertNotNull(answer);
        Assert.assertEquals(answer.getAddress().addr(), "76.75.75.75");
    }

    @Test
    public void testGetExcludedIpsInNetwork() {
        Network network = new NetworkVO(1L, null, null,null, 1L, 1L, 1L, 1L,
                null, null, null,null, 1L, null, null,
                false, null, false, null,null, "10.0.0.1-10.0.0.3,10.0.1.5");
        List<String> res = modelImpl.getExcludedIpsInNetwork(network);
        org.junit.Assert.assertTrue(!res.isEmpty());
        org.junit.Assert.assertTrue(res.contains("10.0.0.2"));
        org.junit.Assert.assertTrue(res.size() == 4);

    }

    @Test
    public void testGetAvailableIps() {
        Network network = new NetworkVO(1L, null, null,null, 1L, 1L, 1L, 1L,
                null, null, null,null, 1L, null, null,
                false, null, false, null,null, "10.0.0.1-10.0.0.3,10.0.0.5");
        ((NetworkVO)network).setCidr("10.0.0.0/29");

        final NicDao nicDao = mock(NicDao.class);
        modelImpl._nicDao = nicDao;
        final NicSecondaryIpDao nicSecondaryIpDao = mock(NicSecondaryIpDao.class);
        modelImpl._nicSecondaryIpDao = nicSecondaryIpDao;
        final ApplicationLoadBalancerRuleDao appLbRuleDao = mock(ApplicationLoadBalancerRuleDao.class);
        modelImpl._appLbRuleDao = appLbRuleDao;

        final List<String> fakeList = new ArrayList<>();
        final SearchBuilder<IPAddressVO> fakeSearch = mock(SearchBuilder.class);
        when(fakeSearch.create()).thenReturn(mock(SearchCriteria.class));
        when(nicDao.search(any(SearchCriteria.class), (Filter) org.mockito.Matchers.isNull())).thenReturn(fakeList);
        when(nicSecondaryIpDao.search(any(SearchCriteria.class), (Filter) org.mockito.Matchers.isNull())).thenReturn(fakeList);
        when(appLbRuleDao.search(any(SearchCriteria.class), (Filter) org.mockito.Matchers.isNull())).thenReturn(fakeList);

        SortedSet<Long> possibleAddresses = modelImpl.getAvailableIps(network, "10.0.0.5");
        org.junit.Assert.assertNull(possibleAddresses);

        possibleAddresses = modelImpl.getAvailableIps(network, "10.0.0.6");
        org.junit.Assert.assertEquals(possibleAddresses.size(), 2);

        network = new NetworkVO(1L, null, null,null, 1L, 1L, 1L, 1L,
                null, null, null,null, 1L, null, null,
                false, null, false, null,null, null);
        ((NetworkVO)network).setCidr("10.0.0.0/29");
        possibleAddresses = modelImpl.getAvailableIps(network, "10.0.0.6");
        org.junit.Assert.assertEquals(possibleAddresses.size(), 6);

    }
}
