package org.apache.cloudstack.secondarystorage;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SecondaryStorageManagerTest {
    @Mock
    DataCenterDao _dcDao;

    @Mock
    NetworkDao _networkDao;

    @InjectMocks
    SecondaryStorageManagerImpl _ssMgr = new SecondaryStorageManagerImpl();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getDefaultNetworkForAdvancedNonSG() {
        final DataCenterVO dc = mock(DataCenterVO.class);
        when(dc.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(dc.isSecurityGroupEnabled()).thenReturn(false);

        when(_dcDao.findById(Mockito.anyLong())).thenReturn(dc);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Public)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Public))))
                .thenReturn(Collections.singletonList(badNetwork));

        when(_networkDao.listByZoneSecurityGroup(anyLong()))
                .thenReturn(Collections.singletonList(badNetwork));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForAdvancedZone(dc);

        Assert.assertNotNull(returnedNetwork);
        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }

    @Test
    public void getDefaultNetworkForAdvancedSG() {
        final DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        when(dc.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(dc.isSecurityGroupEnabled()).thenReturn(true);

        when(_dcDao.findById(Mockito.anyLong())).thenReturn(dc);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), any(TrafficType.class)))
                .thenReturn(Collections.singletonList(badNetwork));

        when(_networkDao.listByZoneSecurityGroup(anyLong()))
                .thenReturn(Collections.singletonList(network));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForAdvancedZone(dc);

        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }

    @Test
    public void getDefaultNetworkForBasicNonSG() {
        final DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        when(dc.getNetworkType()).thenReturn(NetworkType.Basic);
        when(dc.isSecurityGroupEnabled()).thenReturn(false);

        when(_dcDao.findById(Mockito.anyLong())).thenReturn(dc);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Guest)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Guest))))
                .thenReturn(Collections.singletonList(badNetwork));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForBasicZone(dc);

        Assert.assertNotNull(returnedNetwork);
        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }

    @Test
    public void getDefaultNetworkForBasicSG() {
        final DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        when(dc.getNetworkType()).thenReturn(NetworkType.Basic);
        when(dc.isSecurityGroupEnabled()).thenReturn(true);

        when(_dcDao.findById(Mockito.anyLong())).thenReturn(dc);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Guest)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Guest))))
                .thenReturn(Collections.singletonList(badNetwork));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForBasicZone(dc);

        Assert.assertNotNull(returnedNetwork);
        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }

    //also test invalid input
    @Test(expected = CloudRuntimeException.class)
    public void getDefaultNetworkForBasicSGWrongZoneType() {
        final DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        when(dc.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(dc.isSecurityGroupEnabled()).thenReturn(true);

        when(_dcDao.findById(Mockito.anyLong())).thenReturn(dc);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Guest)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Guest))))
                .thenReturn(Collections.singletonList(badNetwork));

        _ssMgr.getDefaultNetworkForBasicZone(dc);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getDefaultNetworkForAdvancedWrongZoneType() {
        final DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        when(dc.getNetworkType()).thenReturn(NetworkType.Basic);
        when(dc.isSecurityGroupEnabled()).thenReturn(true);

        when(_dcDao.findById(Mockito.anyLong())).thenReturn(dc);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), any(TrafficType.class)))
                .thenReturn(Collections.singletonList(badNetwork));

        when(_networkDao.listByZoneSecurityGroup(anyLong()))
                .thenReturn(Collections.singletonList(network));

        _ssMgr.getDefaultNetworkForAdvancedZone(dc);
    }
}
