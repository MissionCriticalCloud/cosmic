package com.cloud.storage.secondary;

import static org.mockito.AdditionalMatchers.not;
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

import java.util.Collections;
import java.util.Optional;

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
    public void getDefaultNetwork() {
        final Zone zone = mock(Zone.class);
        when(zone.getNetworkType()).thenReturn(NetworkType.Advanced);
        when(zoneRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(zone));

        final NetworkVO network = Mockito.mock(NetworkVO.class);
        final NetworkVO badNetwork = Mockito.mock(NetworkVO.class);
        when(_networkDao.listByZoneAndTrafficType(anyLong(), eq(TrafficType.Public)))
                .thenReturn(Collections.singletonList(network));

        when(_networkDao.listByZoneAndTrafficType(anyLong(), not(eq(TrafficType.Public))))
                .thenReturn(Collections.singletonList(badNetwork));

        final NetworkVO returnedNetwork = _ssMgr.getDefaultNetworkForAdvancedZone(zone);

        Assert.assertNotNull(returnedNetwork);
        Assert.assertEquals(network, returnedNetwork);
        Assert.assertNotEquals(badNetwork, returnedNetwork);
    }
}
