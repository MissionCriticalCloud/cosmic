package com.cloud.utils.db;

import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.PreparedStatement;

public class DbTestDao extends GenericDaoBase<DbTestVO, Long> implements GenericDao<DbTestVO, Long> {
    protected DbTestDao() {
    }

    @DB
    public void create(final int fldInt, final long fldLong, final String fldString) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement("insert into cloud.test(fld_int, fld_long, fld_string) values(?, ?, ?)");
            pstmt.setInt(1, fldInt);
            pstmt.setLong(2, fldLong);
            pstmt.setString(3, fldString);

            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception e) {
            throw new CloudRuntimeException("Problem with creating a record in test table", e);
        }
    }

    @DB
    public void update(final int fldInt, final long fldLong, final String fldString) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement("update cloud.test set fld_int=?, fld_long=? where fld_string=?");
            pstmt.setInt(1, fldInt);
            pstmt.setLong(2, fldLong);
            pstmt.setString(3, fldString);

            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception e) {
            throw new CloudRuntimeException("Problem with creating a record in test table", e);
        }
    }
}
