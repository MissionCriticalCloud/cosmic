package com.cloud.network.security.dao;

import com.cloud.network.security.VmRulesetLogVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLTransactionRollbackException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VmRulesetLogDaoImpl extends GenericDaoBase<VmRulesetLogVO, Long> implements VmRulesetLogDao {
    protected static final Logger s_logger = LoggerFactory.getLogger(VmRulesetLogDaoImpl.class);
    final static private int cacheStringSizes[] = {512, 256, 128, 64, 32, 16, 8, 4, 2, 1};
    private static final HashMap<Integer, String> cachedPrepStmtStrings = new HashMap<>();

    static {
        //prepare the cache.
        for (final int size : cacheStringSizes) {
            cachedPrepStmtStrings.put(size, createPrepStatementString(size));
        }
    }

    private final SearchBuilder<VmRulesetLogVO> VmIdSearch;
    private final String InsertOrUpdateSQl = "INSERT INTO op_vm_ruleset_log (instance_id, created, logsequence) "
            + " VALUES(?, now(), 1) ON DUPLICATE KEY UPDATE logsequence=logsequence+1";

    protected VmRulesetLogDaoImpl() {
        VmIdSearch = createSearchBuilder();
        VmIdSearch.and("vmId", VmIdSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);

        VmIdSearch.done();
    }

    private static String createPrepStatementString(final int numItems) {
        final StringBuilder builder = new StringBuilder("INSERT INTO op_vm_ruleset_log (instance_id, created, logsequence) VALUES ");
        for (int i = 0; i < numItems - 1; i++) {
            builder.append("(?, now(), 1), ");
        }
        builder.append("(?, now(), 1) ");
        builder.append(" ON DUPLICATE KEY UPDATE logsequence=logsequence+1");
        return builder.toString();
    }

    @Override
    public VmRulesetLogVO findByVmId(final long vmId) {
        final SearchCriteria<VmRulesetLogVO> sc = VmIdSearch.create();
        sc.setParameters("vmId", vmId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public int createOrUpdate(final Set<Long> workItems) {
        //return createOrUpdateUsingBatch(workItems);
        return createOrUpdateUsingMultiInsert(workItems);
    }

    protected int createOrUpdateUsingMultiInsert(final Set<Long> workItems) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        final int size = workItems.size();
        int count = 0;
        final Iterator<Long> workIter = workItems.iterator();
        int remaining = size;
        try {
            for (final int stmtSize : cacheStringSizes) {
                final int numStmts = remaining / stmtSize;
                if (numStmts > 0) {
                    final String pstmt = cachedPrepStmtStrings.get(stmtSize);
                    for (int i = 0; i < numStmts; i++) {
                        final List<Long> vmIds = new ArrayList<>();
                        for (int argIndex = 1; argIndex <= stmtSize; argIndex++) {
                            final Long vmId = workIter.next();
                            vmIds.add(vmId);
                        }
                        final int numUpdated = executeWithRetryOnDeadlock(txn, pstmt, vmIds);
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("Inserted or updated " + numUpdated + " rows");
                        }
                        if (numUpdated > 0) {
                            count += stmtSize;
                        }
                    }
                    remaining = remaining - numStmts * stmtSize;
                }
            }
        } catch (final SQLException sqe) {
            s_logger.warn("Failed to execute multi insert ", sqe);
        }

        return count;
    }

    private int executeWithRetryOnDeadlock(final TransactionLegacy txn, final String pstmt, final List<Long> vmIds) throws SQLException {

        int numUpdated = 0;
        final int maxTries = 3;
        for (int i = 0; i < maxTries; i++) {
            try {
                final PreparedStatement stmtInsert = txn.prepareAutoCloseStatement(pstmt);
                int argIndex = 1;
                for (final Long vmId : vmIds) {
                    stmtInsert.setLong(argIndex++, vmId);
                }
                numUpdated = stmtInsert.executeUpdate();
                i = maxTries;
            } catch (final SQLTransactionRollbackException e1) {
                if (i < maxTries - 1) {
                    final int delayMs = (i + 1) * 1000;
                    s_logger.debug("Caught a deadlock exception while inserting security group rule log, retrying in " + delayMs);
                    try {
                        Thread.sleep(delayMs);
                    } catch (final InterruptedException ie) {
                        s_logger.debug("[ignored] interupted while inserting security group rule log.");
                    }
                } else {
                    s_logger.warn("Caught another deadlock exception while retrying inserting security group rule log, giving up");
                }
            }
        }
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Inserted or updated " + numUpdated + " rows");
        }
        return numUpdated;
    }

    protected int createOrUpdateUsingBatch(final Set<Long> workItems) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement stmtInsert = null;
        int[] queryResult = null;
        int count = 0;
        boolean success = true;
        try {
            stmtInsert = txn.prepareAutoCloseStatement(InsertOrUpdateSQl);

            txn.start();
            for (final Long vmId : workItems) {
                stmtInsert.setLong(1, vmId);
                stmtInsert.addBatch();
                count++;
                if (count % 16 == 0) {
                    queryResult = stmtInsert.executeBatch();
                    stmtInsert.clearBatch();
                }
            }
            queryResult = stmtInsert.executeBatch();

            txn.commit();
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Updated or inserted " + workItems.size() + " log items");
            }
        } catch (final SQLException e) {
            s_logger.warn("Failed to execute batch update statement for ruleset log: ", e);
            txn.rollback();
            success = false;
        }
        if (!success && queryResult != null) {
            final Long[] arrayItems = new Long[workItems.size()];
            workItems.toArray(arrayItems);
            for (int i = 0; i < queryResult.length; i++) {
                if (queryResult[i] < 0) {
                    s_logger.debug("Batch query update failed for vm " + arrayItems[i]);
                }
            }
        }
        return count;
    }
}
