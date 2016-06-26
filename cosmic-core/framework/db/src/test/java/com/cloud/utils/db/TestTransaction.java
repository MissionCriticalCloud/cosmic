package com.cloud.utils.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestTransaction {

    TransactionLegacy txn;
    Connection conn;

    @Before
    public void setup() {
        setup(TransactionLegacy.CLOUD_DB);
    }

    public void setup(final short db) {
        txn = TransactionLegacy.open(db);
        conn = Mockito.mock(Connection.class);
        txn.setConnection(conn);
    }

    @After
    public void after() {
        TransactionLegacy.currentTxn().close();
    }

    @Test
    public void testCommit() throws Exception {
        assertEquals(42L, Transaction.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(final TransactionStatus status) {
                return 42L;
            }
        }));

        verify(conn).setAutoCommit(false);
        verify(conn, times(1)).commit();
        verify(conn, times(0)).rollback();
        verify(conn, times(1)).close();
    }

    @Test
    public void testRollback() throws Exception {
        try {
            Transaction.execute(new TransactionCallback<Object>() {
                @Override
                public Object doInTransaction(final TransactionStatus status) {
                    throw new RuntimeException("Panic!");
                }
            });
            fail();
        } catch (final RuntimeException e) {
            assertEquals("Panic!", e.getMessage());
        }

        verify(conn).setAutoCommit(false);
        verify(conn, times(0)).commit();
        verify(conn, times(1)).rollback();
        verify(conn, times(1)).close();
    }

    @Test
    public void testRollbackWithException() throws Exception {
        try {
            Transaction.execute(new TransactionCallbackWithException<Object, FileNotFoundException>() {
                @Override
                public Object doInTransaction(final TransactionStatus status) throws FileNotFoundException {
                    assertEquals(TransactionLegacy.CLOUD_DB, TransactionLegacy.currentTxn().getDatabaseId().shortValue());

                    throw new FileNotFoundException("Panic!");
                }
            });
            fail();
        } catch (final FileNotFoundException e) {
            assertEquals("Panic!", e.getMessage());
        }

        verify(conn).setAutoCommit(false);
        verify(conn, times(0)).commit();
        verify(conn, times(1)).rollback();
        verify(conn, times(1)).close();
    }

    @Test
    public void testWithExceptionNoReturn() throws Exception {
        final AtomicInteger i = new AtomicInteger(0);
        assertTrue(Transaction.execute(new TransactionCallbackWithExceptionNoReturn<FileNotFoundException>() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) throws FileNotFoundException {
                i.incrementAndGet();
            }
        }));

        assertEquals(1, i.get());
        verify(conn).setAutoCommit(false);
        verify(conn, times(1)).commit();
        verify(conn, times(0)).rollback();
        verify(conn, times(1)).close();
    }
}
