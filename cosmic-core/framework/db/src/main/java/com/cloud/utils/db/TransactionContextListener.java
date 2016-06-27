package com.cloud.utils.db;

import org.apache.cloudstack.managed.context.ManagedContextListener;

public class TransactionContextListener implements ManagedContextListener<TransactionLegacy> {

    @Override
    public TransactionLegacy onEnterContext(final boolean reentry) {
        if (!reentry) {
            return TransactionLegacy.open(Thread.currentThread().getName());
        }

        return null;
    }

    @Override
    public void onLeaveContext(final TransactionLegacy data, final boolean reentry) {
        if (!reentry) {
            data.close();
        }
    }
}
