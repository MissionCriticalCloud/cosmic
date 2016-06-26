package com.cloud.event.dao;

import com.cloud.dc.Vlan;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsageEventDaoImpl extends GenericDaoBase<UsageEventVO, Long> implements UsageEventDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UsageEventDaoImpl.class.getName());
    private static final String COPY_EVENTS =
            "INSERT INTO cloud_usage.usage_event (id, type, account_id, created, zone_id, resource_id, resource_name, offering_id, template_id, size, resource_type, virtual_size) "
                    + "SELECT id, type, account_id, created, zone_id, resource_id, resource_name, offering_id, template_id, size, resource_type, virtual_size FROM cloud" +
                    ".usage_event vmevt WHERE vmevt.id > ? and vmevt.id <= ? ";
    private static final String COPY_ALL_EVENTS =
            "INSERT INTO cloud_usage.usage_event (id, type, account_id, created, zone_id, resource_id, resource_name, offering_id, template_id, size, resource_type, virtual_size) "
                    + "SELECT id, type, account_id, created, zone_id, resource_id, resource_name, offering_id, template_id, size, resource_type, virtual_size FROM cloud" +
                    ".usage_event vmevt WHERE vmevt.id <= ?";
    private static final String COPY_EVENT_DETAILS = "INSERT INTO cloud_usage.usage_event_details (id, usage_event_id, name, value) "
            + "SELECT id, usage_event_id, name, value FROM cloud.usage_event_details vmevtDetails WHERE vmevtDetails.usage_event_id > ? and vmevtDetails.usage_event_id <= ? ";
    private static final String COPY_ALL_EVENT_DETAILS = "INSERT INTO cloud_usage.usage_event_details (id, usage_event_id, name, value) "
            + "SELECT id, usage_event_id, name, value FROM cloud.usage_event_details vmevtDetails WHERE vmevtDetails.usage_event_id <= ?";
    private static final String MAX_EVENT = "select max(id) from cloud.usage_event where created <= ?";
    private final SearchBuilder<UsageEventVO> latestEventsSearch;
    private final SearchBuilder<UsageEventVO> IpeventsSearch;
    @Inject
    protected UsageEventDetailsDao usageEventDetailsDao;

    public UsageEventDaoImpl() {
        latestEventsSearch = createSearchBuilder();
        latestEventsSearch.and("processed", latestEventsSearch.entity().isProcessed(), SearchCriteria.Op.EQ);
        latestEventsSearch.and("enddate", latestEventsSearch.entity().getCreateDate(), SearchCriteria.Op.LTEQ);
        latestEventsSearch.done();

        IpeventsSearch = createSearchBuilder();
        IpeventsSearch.and("startdate", IpeventsSearch.entity().getCreateDate(), SearchCriteria.Op.GTEQ);
        IpeventsSearch.and("enddate", IpeventsSearch.entity().getCreateDate(), SearchCriteria.Op.LTEQ);
        IpeventsSearch.and("zoneid", IpeventsSearch.entity().getZoneId(), SearchCriteria.Op.EQ);
        IpeventsSearch.and("networktype", IpeventsSearch.entity().getResourceType(), SearchCriteria.Op.EQ);
        IpeventsSearch.and().op("assignEvent", IpeventsSearch.entity().getType(), SearchCriteria.Op.EQ);
        IpeventsSearch.or("releaseEvent", IpeventsSearch.entity().getType(), SearchCriteria.Op.EQ);
        IpeventsSearch.cp();
        IpeventsSearch.done();
    }

    @Override
    public List<UsageEventVO> listLatestEvents(final Date endDate) {
        final Filter filter = new Filter(UsageEventVO.class, "createDate", Boolean.TRUE, null, null);
        final SearchCriteria<UsageEventVO> sc = latestEventsSearch.create();
        sc.setParameters("processed", false);
        sc.setParameters("enddate", endDate);
        return listBy(sc, filter);
    }

    @Override
    public List<UsageEventVO> getLatestEvent() {
        final Filter filter = new Filter(UsageEventVO.class, "id", Boolean.FALSE, Long.valueOf(0), Long.valueOf(1));
        return listAll(filter);
    }

    @Override
    @DB
    public synchronized List<UsageEventVO> getRecentEvents(final Date endDate) {
        final long recentEventId = getMostRecentEventId();
        final long maxEventId = getMaxEventId(endDate);
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        // Copy events from cloud db to usage db
        String sql = COPY_EVENTS;
        if (recentEventId == 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("no recent event date, copying all events");
            }
            sql = COPY_ALL_EVENTS;
        }

        PreparedStatement pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement(sql);
            int i = 1;
            if (recentEventId != 0) {
                pstmt.setLong(i++, recentEventId);
            }
            pstmt.setLong(i++, maxEventId);
            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error copying events from cloud db to usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            txn.close();
        }

        // Copy event details from cloud db to usage db
        sql = COPY_EVENT_DETAILS;
        if (recentEventId == 0) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("no recent event date, copying all event detailss");
            }
            sql = COPY_ALL_EVENT_DETAILS;
        }

        pstmt = null;
        try {
            txn.start();
            pstmt = txn.prepareAutoCloseStatement(sql);
            int i = 1;
            if (recentEventId != 0) {
                pstmt.setLong(i++, recentEventId);
            }
            pstmt.setLong(i++, maxEventId);
            pstmt.executeUpdate();
            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error copying event details from cloud db to usage db", ex);
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            txn.close();
        }

        return findRecentEvents(endDate);
    }

    @DB
    private long getMostRecentEventId() {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        try {
            final List<UsageEventVO> latestEvents = getLatestEvent();

            if (latestEvents != null && latestEvents.size() == 1) {
                final UsageEventVO latestEvent = latestEvents.get(0);
                if (latestEvent != null) {
                    return latestEvent.getId();
                }
            }
            return 0;
        } catch (final Exception ex) {
            s_logger.error("error getting most recent event id", ex);
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            txn.close();
        }
    }

    private List<UsageEventVO> findRecentEvents(final Date endDate) {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        try {
            return listLatestEvents(endDate);
        } catch (final Exception ex) {
            s_logger.error("error getting most recent event date", ex);
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            txn.close();
        }
    }

    private long getMaxEventId(final Date endDate) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            final String sql = MAX_EVENT;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), endDate));
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Long.valueOf(rs.getLong(1));
            }
            return 0;
        } catch (final Exception ex) {
            s_logger.error("error getting max event id", ex);
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            txn.close();
        }
    }

    @Override
    public List<UsageEventVO> listDirectIpEvents(final Date startDate, final Date endDate, final long zoneId) {
        final Filter filter = new Filter(UsageEventVO.class, "createDate", Boolean.TRUE, null, null);
        final SearchCriteria<UsageEventVO> sc = IpeventsSearch.create();
        sc.setParameters("startdate", startDate);
        sc.setParameters("enddate", endDate);
        sc.setParameters("assignEvent", EventTypes.EVENT_NET_IP_ASSIGN);
        sc.setParameters("releaseEvent", EventTypes.EVENT_NET_IP_RELEASE);
        sc.setParameters("zoneid", zoneId);
        sc.setParameters("networktype", Vlan.VlanType.DirectAttached.toString());
        return listBy(sc, filter);
    }

    @Override
    public void saveDetails(final long eventId, final Map<String, String> details) {
        usageEventDetailsDao.persist(eventId, details);
    }
}
