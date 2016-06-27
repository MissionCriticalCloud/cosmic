package com.cloud.configuration;

import static org.mockito.Mockito.when;

import com.cloud.dc.VlanVO;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ValidateIpRangeTest {
    @Mock
    NetworkModel _networkModel;
    @Mock
    VlanVO vlan;
    @Mock
    Network network;
    ConfigurationManagerImpl configurationMgr = new ConfigurationManagerImpl();
    List<VlanVO> vlanVOList = new ArrayList<>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        configurationMgr._networkModel = _networkModel;
        vlanVOList.add(vlan);
        when(vlan.getVlanGateway()).thenReturn("10.147.33.1");
        when(vlan.getVlanNetmask()).thenReturn("255.255.255.128");
    }

    @Test
    public void SameSubnetTest() {
        final Pair<Boolean, Pair<String, String>> sameSubnet =
                configurationMgr.validateIpRange("10.147.33.104", "10.147.33.105", "10.147.33.1", "255.255.255.128", vlanVOList, true, false, null, null, null, null, network);
        Assert.assertTrue(sameSubnet.first());
    }

    @Test
    public void NewSubnetTest() {
        final Pair<Boolean, Pair<String, String>> sameSubnet =
                configurationMgr.validateIpRange("10.147.33.140", "10.147.33.145", "10.147.33.130", "255.255.255.192", vlanVOList, true, false, null, null, null, null,
                        network);
        Assert.assertTrue(!sameSubnet.first());
    }

    @Test
    public void SuperSetTest() {
        try {
            configurationMgr.validateIpRange("10.147.33.10", "10.147.33.20", "10.147.33.21", "255.255.255.0", vlanVOList, true, false, null, null, null, null, network);
        } catch (final Exception e) {
            junit.framework.Assert.assertTrue(e.getMessage().contains("superset"));
        }
    }
}
