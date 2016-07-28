package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.DateUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.framework.jobs.impl.SyncQueueItemVO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DB
public class SyncQueueItemDaoImpl extends GenericDaoBase<SyncQueueItemVO, Long> implements SyncQueueItemDao {
    private static final Logger s_logger = LoggerFactory.getLogger(SyncQueueItemDaoImpl.class);
    final GenericSearchBuilder<SyncQueueItemVO, Long> queueIdSearch;
    final GenericSearchBuilder<SyncQueueItemVO, Integer> queueActiveItemSearch;

    public SyncQueueItemDaoImpl() {
        super();
        queueIdSearch = createSearchBuilder(Long.class);
        queueIdSearch.and("contentId", queueIdSearch.entity().getContentId(), Op.EQ);
        queueIdSearch.and("contentType", queueIdSearch.entity().getContentType(), Op.EQ);
        queueIdSearch.selectFields(queueIdSearch.entity().getId());
        queueIdSearch.done();

        queueActiveItemSearch = createSearchBuilder(Integer.class);
        queueActiveItemSearch.and("queueId", queueActiveItemSearch.entity().getQueueId(), Op.EQ);
        queueActiveItemSearch.and("processNumber", queueActiveItemSearch.entity().getLastProcessNumber(), Op.NNULL);
        queueActiveItemSearch.select(null, Func.COUNT, queueActiveItemSearch.entity().getId());
        queueActiveItemSearch.done();
    }

    @Override
    public SyncQueueItemVO getNextQueueItem(final long queueId) {

        final SearchBuilder<SyncQueueItemVO> sb = createSearchBuilder();
        sb.and("queueId", sb.entity().getQueueId(), SearchCriteria.Op.EQ);
        sb.and("lastProcessNumber", sb.entity().getLastProcessNumber(), SearchCriteria.Op.NULL);
        sb.done();

        final SearchCriteria<SyncQueueItemVO> sc = sb.create();
        sc.setParameters("queueId", queueId);

        final Filter filter = new Filter(SyncQueueItemVO.class, "created", true, 0L, 1L);
        final List<SyncQueueItemVO> l = listBy(sc, filter);
        if (l != null && l.size() > 0) {
            return l.get(0);
        }

        return null;
    }

    @Override
    public int getActiveQueueItemCount(final long queueId) {
        final SearchCriteria<Integer> sc = queueActiveItemSearch.create();
        sc.setParameters("queueId", queueId);

        final List<Integer> count = customSearch(sc, null);
        return count.get(0);
    }

    @Override
    public List<SyncQueueItemVO> getNextQueueItems(final int maxItems) {
        final List<SyncQueueItemVO> l = new ArrayList<>();

        final String sql = "SELECT i.id, i.queue_id, i.content_type, i.content_id, i.created " +
                " FROM sync_queue AS q JOIN sync_queue_item AS i ON q.id = i.queue_id " +
                " WHERE i.queue_proc_number IS NULL " +
                " GROUP BY q.id " +
                " ORDER BY i.id " +
                " LIMIT 0, ?";

        final PreparedStatement pstmt;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setInt(1, maxItems);
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                final SyncQueueItemVO item = new SyncQueueItemVO();
                item.setId(rs.getLong(1));
                item.setQueueId(rs.getLong(2));
                item.setContentType(rs.getString(3));
                item.setContentId(rs.getLong(4));
                item.setCreated(DateUtil.parseDateString(TimeZone.getTimeZone("GMT"), rs.getString(5)));
                l.add(item);
            }
        } catch (final SQLException e) {
            s_logger.error("Unexpected sql exception, ", e);
        }
        return l;
    }

    @Override
    public List<SyncQueueItemVO> getActiveQueueItems(final Long msid, final boolean exclusive) {
        final SearchBuilder<SyncQueueItemVO> sb = createSearchBuilder();
        sb.and("lastProcessMsid", sb.entity().getLastProcessMsid(),
                SearchCriteria.Op.EQ);
        sb.done();

        final SearchCriteria<SyncQueueItemVO> sc = sb.create();
        sc.setParameters("lastProcessMsid", msid);

        final Filter filter = new Filter(SyncQueueItemVO.class, "created", true, null, null);

        if (exclusive) {
            return lockRows(sc, filter, true);
        }
        return listBy(sc, filter);
    }

    @Override
    public List<SyncQueueItemVO> getBlockedQueueItems(final long thresholdMs, final boolean exclusive) {
        final Date cutTime = DateUtil.currentGMTTime();

        final SearchBuilder<SyncQueueItemVO> sbItem = createSearchBuilder();
        sbItem.and("lastProcessMsid", sbItem.entity().getLastProcessMsid(), SearchCriteria.Op.NNULL);
        sbItem.and("lastProcessNumber", sbItem.entity().getLastProcessNumber(), SearchCriteria.Op.NNULL);
        sbItem.and("lastProcessTime", sbItem.entity().getLastProcessTime(), SearchCriteria.Op.NNULL);
        sbItem.and("lastProcessTime2", sbItem.entity().getLastProcessTime(), SearchCriteria.Op.LT);

        sbItem.done();

        final SearchCriteria<SyncQueueItemVO> sc = sbItem.create();
        sc.setParameters("lastProcessTime2", new Date(cutTime.getTime() - thresholdMs));

        if (exclusive) {
            return lockRows(sc, null, true);
        }
        return listBy(sc, null);
    }

    @Override
    public Long getQueueItemIdByContentIdAndType(final long contentId, final String contentType) {
        final SearchCriteria<Long> sc = queueIdSearch.create();
        sc.setParameters("contentId", contentId);
        sc.setParameters("contentType", contentType);
        final List<Long> id = customSearch(sc, null);

        return id.size() == 0 ? null : id.get(0);
    }
}
