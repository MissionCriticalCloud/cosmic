package com.cloud.storage.secondary;

import com.cloud.utils.component.Adapter;
import com.cloud.vm.SecondaryStorageVmVO;

import java.util.List;
import java.util.Map;

public interface SecondaryStorageVmAllocator extends Adapter {
    public SecondaryStorageVmVO allocSecondaryStorageVm(List<SecondaryStorageVmVO> candidates, Map<Long, Integer> loadInfo, long dataCenterId);
}
