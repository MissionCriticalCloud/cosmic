package com.cloud.utils.db;

/**
 * TransactionAttachment are objects added to Transaction such that when
 * the in memory transaction is closed, they are automatically closed.
 */
public interface TransactionAttachment {
    /**
     * @return a unique name to be inserted.
     */
    String getName();

    /**
     * cleanup() if it wasn't cleaned up before.
     */
    void cleanup();
}
