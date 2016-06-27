package com.cloud.utils.db;

import java.util.List;
import java.util.Map;

public interface TransactionMBean {
    int getTransactionCount();

    int[] getActiveTransactionCount();

    List<Map<String, String>> getTransactions();

    List<Map<String, String>> getActiveTransactions();

    List<Map<String, String>> getTransactionsWithDatabaseConnection();
}
