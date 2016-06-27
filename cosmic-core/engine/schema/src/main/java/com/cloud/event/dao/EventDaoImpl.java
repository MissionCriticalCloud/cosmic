package com.cloud.event.dao;

import com.cloud.event.Event.State;
import com.cloud.event.EventVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventDaoImpl extends GenericDaoBase<EventVO, Long> implements EventDao {
    public static final Logger s_logger = LoggerFactory.getLogger(EventDaoImpl.class.getName());
    protected final SearchBuilder<EventVO> CompletedEventSearch;
    protected final SearchBuilder<EventVO> ToArchiveOrDeleteEventSearch;

    public EventDaoImpl() {
        CompletedEventSearch = createSearchBuilder();
        CompletedEventSearch.and("state", CompletedEventSearch.entity().getState(), SearchCriteria.Op.EQ);
        CompletedEventSearch.and("startId", CompletedEventSearch.entity().getStartId(), SearchCriteria.Op.EQ);
        CompletedEventSearch.and("archived", CompletedEventSearch.entity().getArchived(), Op.EQ);
        CompletedEventSearch.done();

        ToArchiveOrDeleteEventSearch = createSearchBuilder();
        ToArchiveOrDeleteEventSearch.and("id", ToArchiveOrDeleteEventSearch.entity().getId(), Op.IN);
        ToArchiveOrDeleteEventSearch.and("type", ToArchiveOrDeleteEventSearch.entity().getType(), Op.EQ);
        ToArchiveOrDeleteEventSearch.and("accountIds", ToArchiveOrDeleteEventSearch.entity().getAccountId(), Op.IN);
        ToArchiveOrDeleteEventSearch.and("createdDateB", ToArchiveOrDeleteEventSearch.entity().getCreateDate(), Op.BETWEEN);
        ToArchiveOrDeleteEventSearch.and("createdDateL", ToArchiveOrDeleteEventSearch.entity().getCreateDate(), Op.LTEQ);
        ToArchiveOrDeleteEventSearch.and("archived", ToArchiveOrDeleteEventSearch.entity().getArchived(), Op.EQ);
        ToArchiveOrDeleteEventSearch.done();
    }

    @Override
    public List<EventVO> searchAllEvents(final SearchCriteria<EventVO> sc, final Filter filter) {
        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    public List<EventVO> listOlderEvents(final Date oldTime) {
        if (oldTime == null) {
            return null;
        }
        final SearchCriteria<EventVO> sc = createSearchCriteria();
        sc.addAnd("createDate", SearchCriteria.Op.LT, oldTime);
        sc.addAnd("archived", SearchCriteria.Op.EQ, false);
        return listIncludingRemovedBy(sc, null);
    }

    @Override
    public EventVO findCompletedEvent(final long startId) {
        final SearchCriteria<EventVO> sc = CompletedEventSearch.create();
        sc.setParameters("state", State.Completed);
        sc.setParameters("startId", startId);
        sc.setParameters("archived", false);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public List<EventVO> listToArchiveOrDeleteEvents(final List<Long> ids, final String type, final Date startDate, final Date endDate, final List<Long> accountIds) {
        final SearchCriteria<EventVO> sc = ToArchiveOrDeleteEventSearch.create();
        if (ids != null) {
            sc.setParameters("id", ids.toArray(new Object[ids.size()]));
        }
        if (type != null) {
            sc.setParameters("type", type);
        }
        if (startDate != null && endDate != null) {
            sc.setParameters("createdDateB", startDate, endDate);
        } else if (endDate != null) {
            sc.setParameters("createdDateL", endDate);
        }
        if (accountIds != null && !accountIds.isEmpty()) {
            sc.setParameters("accountIds", accountIds.toArray(new Object[accountIds.size()]));
        }
        sc.setParameters("archived", false);
        return search(sc, null);
    }

    @Override
    public void archiveEvents(final List<EventVO> events) {
        if (events != null && !events.isEmpty()) {
            final TransactionLegacy txn = TransactionLegacy.currentTxn();
            txn.start();
            for (EventVO event : events) {
                event = lockRow(event.getId(), true);
                event.setArchived(true);
                update(event.getId(), event);
                txn.commit();
            }
            txn.close();
        }
    }
}
