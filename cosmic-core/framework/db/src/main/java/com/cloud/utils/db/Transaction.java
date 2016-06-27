package com.cloud.utils.db;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transaction {
    private final static AtomicLong counter = new AtomicLong(0);
    private final static TransactionStatus STATUS = new TransactionStatus() {
    };

    private static final Logger s_logger = LoggerFactory.getLogger(Transaction.class);

    public static <T> T execute(final TransactionCallback<T> callback) {
        return execute(new TransactionCallbackWithException<T, RuntimeException>() {
            @Override
            public T doInTransaction(final TransactionStatus status) throws RuntimeException {
                return callback.doInTransaction(status);
            }
        });
    }

    public static <T, E extends Throwable> T execute(final TransactionCallbackWithException<T, E> callback) throws E {
        final String name = "tx-" + counter.incrementAndGet();
        short databaseId = TransactionLegacy.CLOUD_DB;
        final TransactionLegacy currentTxn = TransactionLegacy.currentTxn(false);
        if (currentTxn != null) {
            databaseId = currentTxn.getDatabaseId();
        }
        try (final TransactionLegacy txn = TransactionLegacy.open(name, databaseId, false)) {
            txn.start();
            final T result = callback.doInTransaction(STATUS);
            txn.commit();
            return result;
        }
    }

    public static <T> T execute(final short databaseId, final TransactionCallback<T> callback) {
        return execute(databaseId, new TransactionCallbackWithException<T, RuntimeException>() {
            @Override
            public T doInTransaction(final TransactionStatus status) throws RuntimeException {
                return callback.doInTransaction(status);
            }
        });
    }

    public static <T, E extends Throwable> T execute(final short databaseId, final TransactionCallbackWithException<T, E> callback) throws E {
        final String name = "tx-" + counter.incrementAndGet();
        final TransactionLegacy currentTxn = TransactionLegacy.currentTxn(false);
        final short outer_txn_databaseId = (currentTxn != null ? currentTxn.getDatabaseId() : databaseId);
        try (final TransactionLegacy txn = TransactionLegacy.open(name, databaseId, true)) {
            txn.start();
            final T result = callback.doInTransaction(STATUS);
            txn.commit();
            return result;
        } finally {
            TransactionLegacy.open(outer_txn_databaseId).close();
        }
    }
}
