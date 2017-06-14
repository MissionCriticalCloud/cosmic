package com.cloud.storage.secondary;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.model.enumeration.NetworkType;
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
    NetworkDao _networkDao;

    @Mock
    ZoneRepository zoneRepository;

    @InjectMocks
    SecondaryStorageManagerImpl _ssMgr = new SecondaryStorageManagerImpl();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getDefaultNetworkForAdvancedNonSG() {
        final Zone zone = mock(Zone.class);
        when(zone.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(zone.isSecurityGroupEnabled()).thenReturn(false);

        when(zoneRepository.findOne(Mockito.anyLong())).thenReturn(zone);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Public)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Public))))
                .thenReturn(Collections.singletonList(badNetwork));

        when(_networkDao.listByZoneSecurityGroup(anyLong()))
                .thenReturn(Collections.singletonList(badNetwork));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForAdvancedZone(zone);

        Assert.assertNotNull(returnedNetwork);
        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }

    @Test
    public void getDefaultNetworkForAdvancedSG() {
        final Zone zone = mock(Zone.class);
        when(zone.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(zone.isSecurityGroupEnabled()).thenReturn(true);

        when(zoneRepository.findOne(Mockito.anyLong())).thenReturn(zone);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), any(TrafficType.class)))
                .thenReturn(Collections.singletonList(badNetwork));

        when(_networkDao.listByZoneSecurityGroup(anyLong()))
                .thenReturn(Collections.singletonList(network));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForAdvancedZone(zone);

        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }

    @Test
    public void getDefaultNetworkForBasicNonSG() {
        final Zone zone = mock(Zone.class);
        when(zone.getNetworkType()).thenReturn(NetworkType.Basic);
        when(zone.isSecurityGroupEnabled()).thenReturn(false);

        when(zoneRepository.findOne(Mockito.anyLong())).thenReturn(zone);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Guest)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Guest))))
                .thenReturn(Collections.singletonList(badNetwork));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForBasicZone(zone);

        Assert.assertNotNull(returnedNetwork);
        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }

    @Test
    public void getDefaultNetworkForBasicSG() {
        final Zone zone = mock(Zone.class);
        when(zone.getNetworkType()).thenReturn(NetworkType.Basic);
        when(zone.isSecurityGroupEnabled()).thenReturn(true);

        when(zoneRepository.findOne(Mockito.anyLong())).thenReturn(zone);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Guest)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Guest))))
                .thenReturn(Collections.singletonList(badNetwork));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForBasicZone(zone);
        Assert.assertNotNull(returnedNetwork);
        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }

    //also test invalid input
    @Test(expected = CloudRuntimeException.class)
    public void getDefaultNetworkForBasicSGWrongZoneType() {
        final Zone zone = mock(Zone.class);
        when(zone.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(zone.isSecurityGroupEnabled()).thenReturn(true);

        when(zoneRepository.findOne(Mockito.anyLong())).thenReturn(zone);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Guest)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Guest))))
                .thenReturn(Collections.singletonList(badNetwork));

        _ssMgr.getDefaultNetworkForBasicZone(zone);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getDefaultNetworkForAdvancedWrongZoneType() {
        final Zone zone = mock(Zone.class);
        when(zone.getNetworkType()).thenReturn(NetworkType.Basic);
        when(zone.isSecurityGroupEnabled()).thenReturn(true);

        when(zoneRepository.findOne(Mockito.anyLong())).thenReturn(zone);

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), any(TrafficType.class)))
                .thenReturn(Collections.singletonList(badNetwork));

        when(_networkDao.listByZoneSecurityGroup(anyLong()))
                .thenReturn(Collections.singletonList(network));

        _ssMgr.getDefaultNetworkForAdvancedZone(zone);
    }
}
