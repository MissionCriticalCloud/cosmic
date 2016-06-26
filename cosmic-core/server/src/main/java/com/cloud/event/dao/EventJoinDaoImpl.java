package com.cloud.event.dao;

import com.cloud.api.ApiResponseHelper;
import com.cloud.api.query.vo.EventJoinVO;
import com.cloud.event.Event;
import com.cloud.event.Event.State;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.response.EventResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EventJoinDaoImpl extends GenericDaoBase<EventJoinVO, Long> implements EventJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(EventJoinDaoImpl.class);

    private final SearchBuilder<EventJoinVO> vrSearch;

    private final SearchBuilder<EventJoinVO> vrIdSearch;

    private final SearchBuilder<EventJoinVO> CompletedEventSearch;

    protected EventJoinDaoImpl() {

        vrSearch = createSearchBuilder();
        vrSearch.and("idIN", vrSearch.entity().getId(), SearchCriteria.Op.IN);
        vrSearch.done();

        vrIdSearch = createSearchBuilder();
        vrIdSearch.and("id", vrIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        vrIdSearch.done();

        CompletedEventSearch = createSearchBuilder();
        CompletedEventSearch.and("state", CompletedEventSearch.entity().getState(), SearchCriteria.Op.EQ);
        CompletedEventSearch.and("startId", CompletedEventSearch.entity().getStartId(), SearchCriteria.Op.EQ);
        CompletedEventSearch.done();

        this._count = "select count(distinct id) from event_view WHERE ";
    }

    @Override
    public EventResponse newEventResponse(final EventJoinVO event) {
        final EventResponse responseEvent = new EventResponse();
        responseEvent.setCreated(event.getCreateDate());
        responseEvent.setDescription(event.getDescription());
        responseEvent.setEventType(event.getType());
        responseEvent.setId(event.getUuid());
        responseEvent.setLevel(event.getLevel());
        responseEvent.setParentId(event.getStartUuid());
        responseEvent.setState(event.getState());
        responseEvent.setUsername(event.getUserName());

        ApiResponseHelper.populateOwner(responseEvent, event);
        responseEvent.setObjectName("event");
        return responseEvent;
    }

    @Override
    public EventJoinVO newEventView(final Event vr) {

        final SearchCriteria<EventJoinVO> sc = vrIdSearch.create();
        sc.setParameters("id", vr.getId());
        final List<EventJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
        assert vms != null && vms.size() == 1 : "No event found for event id " + vr.getId();
        return vms.get(0);
    }

    @Override
    public List<EventJoinVO> searchByIds(final Long... ids) {
        final SearchCriteria<EventJoinVO> sc = vrSearch.create();
        sc.setParameters("idIN", ids);
        return searchIncludingRemoved(sc, null, null, false);
    }

    @Override
    public List<EventJoinVO> searchAllEvents(final SearchCriteria<EventJoinVO> sc, final Filter filter) {
        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    public EventJoinVO findCompletedEvent(final long startId) {
        final SearchCriteria<EventJoinVO> sc = CompletedEventSearch.create();
        sc.setParameters("state", State.Completed);
        sc.setParameters("startId", startId);
        return findOneIncludingRemovedBy(sc);
    }
}
