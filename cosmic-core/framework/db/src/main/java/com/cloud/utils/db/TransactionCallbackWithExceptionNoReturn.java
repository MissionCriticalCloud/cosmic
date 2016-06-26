package com.cloud.utils.db;

public abstract class TransactionCallbackWithExceptionNoReturn<E extends Throwable> implements TransactionCallbackWithException<Boolean, E> {

    @Override
    public final Boolean doInTransaction(final TransactionStatus status) throws E {
        doInTransactionWithoutResult(status);
        return true;
    }

    public abstract void doInTransactionWithoutResult(TransactionStatus status) throws E;
}
