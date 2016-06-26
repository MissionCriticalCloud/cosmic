package com.cloud.consoleproxy;

import com.cloud.utils.component.Adapter;
import com.cloud.vm.ConsoleProxy;

import java.util.List;
import java.util.Map;

public interface ConsoleProxyAllocator extends Adapter {
    /**
     * Finds the least loaded console proxy.
     *
     * @param candidates
     * @param loadInfo
     * @param dataCenterId
     * @return id of the console proxy to use or null if none.
     */
    public Long allocProxy(List<? extends ConsoleProxy> candidates, Map<Long, Integer> loadInfo, long dataCenterId);
}
