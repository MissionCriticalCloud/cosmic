package com.cloud.consoleproxy;

import com.cloud.utils.component.AdapterBase;
import com.cloud.vm.ConsoleProxy;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ConsoleProxyBalanceAllocator extends AdapterBase implements ConsoleProxyAllocator {

    @Override
    public Long allocProxy(final List<? extends ConsoleProxy> candidates, final Map<Long, Integer> loadInfo, final long dataCenterId) {
        final List<ConsoleProxy> allocationList = new ArrayList<>(candidates);

        Collections.sort(candidates, new Comparator<ConsoleProxy>() {
            @Override
            public int compare(final ConsoleProxy x, final ConsoleProxy y) {
                final Integer loadOfX = loadInfo.get(x.getId());
                final Integer loadOfY = loadInfo.get(y.getId());

                if (loadOfX != null && loadOfY != null) {
                    if (loadOfX < loadOfY) {
                        return -1;
                    } else if (loadOfX > loadOfY) {
                        return 1;
                    }
                    return 0;
                } else if (loadOfX == null && loadOfY == null) {
                    return 0;
                } else {
                    if (loadOfX == null) {
                        return -1;
                    }
                    return 1;
                }
            }
        });

        return (allocationList.size() > 0) ? allocationList.get(0).getId() : null;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
}
