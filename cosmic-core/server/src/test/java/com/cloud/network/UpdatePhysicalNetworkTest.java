package com.cloud.network;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.DataCenterVnetDao;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.utils.db.TransactionLegacy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class UpdatePhysicalNetworkTest {
    private final PhysicalNetworkDao _physicalNetworkDao = mock(PhysicalNetworkDao.class);
    private final DataCenterVnetDao _datacenterVnetDao = mock(DataCenterVnetDao.class);
    private final DataCenterDao _datacenterDao = mock(DataCenterDao.class);
    private final DataCenterVO datacentervo = mock(DataCenterVO.class);
    private final PhysicalNetworkVO physicalNetworkVO = mock(PhysicalNetworkVO.class);
    List<String> existingRange = new ArrayList<>();
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

    @Test
    public void updatePhysicalNetworkTest() {
        final TransactionLegacy txn = TransactionLegacy.open("updatePhysicalNetworkTest");
        final NetworkServiceImpl networkService = setUp();
        existingRange.add("524");
        when(_physicalNetworkDao.findById(anyLong())).thenReturn(physicalNetworkVO);
        when(_datacenterDao.findById(anyLong())).thenReturn(datacentervo);
        when(_physicalNetworkDao.update(anyLong(), any(physicalNetworkVO.getClass()))).thenReturn(true);
        when(_datacenterVnetDao.listVnetsByPhysicalNetworkAndDataCenter(anyLong(), anyLong())).thenReturn(existingRange);
        networkService.updatePhysicalNetwork(1l, null, null, "524-524,525-530", null);
        txn.close("updatePhysicalNetworkTest");
        verify(physicalNetworkVO).setVnet(argumentCaptor.capture());
        assertEquals("524-530", argumentCaptor.getValue());
    }

    public NetworkServiceImpl setUp() {
        final NetworkServiceImpl networkService = new NetworkServiceImpl();
        networkService._dcDao = _datacenterDao;
        networkService._physicalNetworkDao = _physicalNetworkDao;
        networkService._datacneterVnet = _datacenterVnetDao;
        return networkService;
    }
}
