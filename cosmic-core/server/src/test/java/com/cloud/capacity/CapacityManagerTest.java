package com.cloud.capacity;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cloud.capacity.dao.CapacityDao;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterDetailsVO;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.vm.VirtualMachine;

import org.junit.Assert;
import org.junit.Test;

public class CapacityManagerTest {
    CapacityDao CDao = mock(CapacityDao.class);
    ServiceOfferingDao SOfferingDao = mock(ServiceOfferingDao.class);
    ClusterDetailsDao ClusterDetailsDao = mock(com.cloud.dc.ClusterDetailsDao.class);
    CapacityManagerImpl capMgr;
    private final ServiceOfferingVO svo = mock(ServiceOfferingVO.class);
    private final CapacityVO cvoCpu = mock(CapacityVO.class);
    private final CapacityVO cvoRam = mock(CapacityVO.class);
    private final VirtualMachine vm = mock(VirtualMachine.class);
    private final ClusterDetailsVO clusterDetailCpu = mock(ClusterDetailsVO.class);
    private final ClusterDetailsVO clusterDetailRam = mock(ClusterDetailsVO.class);

    @Test
    public void allocateCapacityTest() {
        capMgr = setUp();
        when(vm.getHostId()).thenReturn(1l);
        when(vm.getServiceOfferingId()).thenReturn(2l);
        when(SOfferingDao.findById(anyLong(), anyLong())).thenReturn(svo);
        when(CDao.findByHostIdType(anyLong(), eq(Capacity.CAPACITY_TYPE_CPU))).thenReturn(cvoCpu);
        when(CDao.findByHostIdType(anyLong(), eq(Capacity.CAPACITY_TYPE_MEMORY))).thenReturn(cvoRam);
        when(cvoCpu.getUsedCapacity()).thenReturn(500l);
        when(cvoCpu.getTotalCapacity()).thenReturn(2000l);
        when(cvoRam.getUsedCapacity()).thenReturn(3000l);
        when(cvoRam.getTotalCapacity()).thenReturn((long) 1024 * 1024 * 1024);
        when(svo.getCpu()).thenReturn(500);
        when(svo.getRamSize()).thenReturn(512);
        when(cvoCpu.getReservedCapacity()).thenReturn(0l);
        when(cvoRam.getReservedCapacity()).thenReturn(0l);
        when(clusterDetailRam.getValue()).thenReturn("1.5");
        when(clusterDetailCpu.getValue()).thenReturn("2");
        when(CDao.update(anyLong(), isA(CapacityVO.class))).thenReturn(true);
        final boolean hasCapacity = capMgr.checkIfHostHasCapacity(1l, 500, 1024 * 1024 * 1024, false, 2, 2, false);
        Assert.assertTrue(hasCapacity);
    }

    public CapacityManagerImpl setUp() {
        final CapacityManagerImpl capMgr = new CapacityManagerImpl();
        capMgr._clusterDetailsDao = ClusterDetailsDao;
        capMgr._capacityDao = CDao;
        capMgr._offeringsDao = SOfferingDao;
        return capMgr;
    }
}
