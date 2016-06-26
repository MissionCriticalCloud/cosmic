package com.cloud.utils.db;

import com.cloud.utils.component.ComponentMethodInterceptor;

import java.lang.reflect.Method;

public class TransactionContextBuilder implements ComponentMethodInterceptor {
    public TransactionContextBuilder() {
    }

    @Override
    public boolean needToIntercept(final Method method) {
        DB db = method.getAnnotation(DB.class);
        if (db != null) {
            return true;
        }

        Class<?> clazz = method.getDeclaringClass();

        do {
            db = clazz.getAnnotation(DB.class);
            if (db != null) {
                return true;
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && clazz != null);

        return false;
    }

    @Override
    public Object interceptStart(final Method method, final Object target) {
        return TransactionLegacy.open(method.getName());
    }

    @Override
    public void interceptComplete(final Method method, final Object target, final Object objReturnedInInterceptStart) {
        final TransactionLegacy txn = (TransactionLegacy) objReturnedInInterceptStart;
        if (txn != null) {
            txn.close();
        }
    }

    @Override
    public void interceptException(final Method method, final Object target, final Object objReturnedInInterceptStart) {
        final TransactionLegacy txn = (TransactionLegacy) objReturnedInInterceptStart;
        if (txn != null) {
            txn.close();
        }
    }
}
