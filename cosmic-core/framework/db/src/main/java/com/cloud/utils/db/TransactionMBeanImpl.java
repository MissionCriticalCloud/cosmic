package com.cloud.utils.db;

import com.cloud.utils.db.TransactionLegacy.StackElement;

import javax.management.StandardMBean;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionMBeanImpl extends StandardMBean implements TransactionMBean {

    Map<Long, TransactionLegacy> _txns = new ConcurrentHashMap<>();

    public TransactionMBeanImpl() {
        super(TransactionMBean.class, false);
    }

    public void addTransaction(final TransactionLegacy txn) {
        _txns.put(txn.getId(), txn);
    }

    public void removeTransaction(final TransactionLegacy txn) {
        _txns.remove(txn.getId());
    }

    @Override
    public int getTransactionCount() {
        return _txns.size();
    }

    @Override
    public int[] getActiveTransactionCount() {
        final int[] count = new int[2];
        count[0] = 0;
        count[1] = 0;
        for (final TransactionLegacy txn : _txns.values()) {
            if (txn.getStack().size() > 0) {
                count[0]++;
            }
            if (txn.getCurrentConnection() != null) {
                count[1]++;
            }
        }
        return count;
    }

    @Override
    public List<Map<String, String>> getTransactions() {
        final ArrayList<Map<String, String>> txns = new ArrayList<>();
        for (final TransactionLegacy info : _txns.values()) {
            txns.add(toMap(info));
        }
        return txns;
    }

    @Override
    public List<Map<String, String>> getActiveTransactions() {
        final ArrayList<Map<String, String>> txns = new ArrayList<>();
        for (final TransactionLegacy txn : _txns.values()) {
            if (txn.getStack().size() > 0 || txn.getCurrentConnection() != null) {
                txns.add(toMap(txn));
            }
        }
        return txns;
    }

    @Override
    public List<Map<String, String>> getTransactionsWithDatabaseConnection() {
        final ArrayList<Map<String, String>> txns = new ArrayList<>();
        for (final TransactionLegacy txn : _txns.values()) {
            if (txn.getCurrentConnection() != null) {
                txns.add(toMap(txn));
            }
        }
        return txns;
    }

    protected Map<String, String> toMap(final TransactionLegacy txn) {
        final Map<String, String> map = new HashMap<>();
        map.put("name", txn.getName());
        map.put("id", Long.toString(txn.getId()));
        map.put("creator", txn.getCreator());
        final Connection conn = txn.getCurrentConnection();
        map.put("db", conn != null ? Integer.toString(System.identityHashCode(conn)) : "none");
        final StringBuilder buff = new StringBuilder();
        for (final StackElement element : txn.getStack()) {
            buff.append(element.toString()).append(",");
        }
        map.put("stack", buff.toString());

        return map;
    }
}
