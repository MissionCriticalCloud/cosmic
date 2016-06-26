package com.cloud.storage.secondary;

import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.SecondaryStorageVmVO;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class SecondaryStorageVmDefaultAllocator extends AdapterBase implements SecondaryStorageVmAllocator {

    private final Random _rand = new Random(System.currentTimeMillis());
    private String _name;

    @Override
    public SecondaryStorageVmVO allocSecondaryStorageVm(final List<SecondaryStorageVmVO> candidates, final Map<Long, Integer> loadInfo, final long dataCenterId) {
        if (candidates.size() > 0) {
            return candidates.get(_rand.nextInt(candidates.size()));
        }
        return null;
    }
}
