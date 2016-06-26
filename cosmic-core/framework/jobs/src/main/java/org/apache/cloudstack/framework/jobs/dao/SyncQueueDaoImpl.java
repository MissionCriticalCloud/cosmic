package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.framework.jobs.impl.SyncQueueVO;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncQueueDaoImpl extends GenericDaoBase<SyncQueueVO, Long> implements SyncQueueDao {
    private static final Logger s_logger = LoggerFactory.getLogger(SyncQueueDaoImpl.class.getName());

    SearchBuilder<SyncQueueVO> TypeIdSearch = createSearchBuilder();

    public SyncQueueDaoImpl() {
        super();
        TypeIdSearch = createSearchBuilder();
        TypeIdSearch.and("syncObjType", TypeIdSearch.entity().getSyncObjType(), SearchCriteria.Op.EQ);
        TypeIdSearch.and("syncObjId", TypeIdSearch.entity().getSyncObjId(), SearchCriteria.Op.EQ);
        TypeIdSearch.done();
    }

    @Override
    public void ensureQueue(final String syncObjType, final long syncObjId) {
        final Date dt = DateUtil.currentGMTTime();
        final String sql = "INSERT IGNORE INTO sync_queue(sync_objtype, sync_objid, created, last_updated)" + " values(?, ?, ?, ?)";

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, syncObjType);
            pstmt.setLong(2, syncObjId);
            pstmt.setString(3, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), dt));
            pstmt.setString(4, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), dt));
            pstmt.execute();
        } catch (final SQLException e) {
            s_logger.warn("Unable to create sync queue " + syncObjType + "-" + syncObjId + ":" + e.getMessage(), e);
        } catch (final Throwable e) {
            s_logger.warn("Unable to create sync queue " + syncObjType + "-" + syncObjId + ":" + e.getMessage(), e);
        }
    }

    @Override
    public SyncQueueVO find(final String syncObjType, final long syncObjId) {
        final SearchCriteria<SyncQueueVO> sc = TypeIdSearch.create();
        sc.setParameters("syncObjType", syncObjType);
        sc.setParameters("syncObjId", syncObjId);
        return findOneBy(sc);
    }
}
