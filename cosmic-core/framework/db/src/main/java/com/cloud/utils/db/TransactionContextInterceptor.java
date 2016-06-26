package com.cloud.utils.db;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class TransactionContextInterceptor implements MethodInterceptor {

    public TransactionContextInterceptor() {

    }

    @Override
    public Object invoke(final MethodInvocation m) throws Throwable {
        final TransactionLegacy txn = TransactionLegacy.open(m.getMethod().getName());
        try {
            return m.proceed();
        } finally {
            txn.close();
        }
    }
}
