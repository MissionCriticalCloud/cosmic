package com.cloud.event.dao;

import com.cloud.api.query.vo.EventJoinVO;
import com.cloud.event.Event;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.response.EventResponse;

import java.util.List;

public interface EventJoinDao extends GenericDao<EventJoinVO, Long> {

    EventResponse newEventResponse(EventJoinVO uvo);

    EventJoinVO newEventView(Event vr);

    List<EventJoinVO> searchByIds(Long... ids);

    List<EventJoinVO> searchAllEvents(SearchCriteria<EventJoinVO> sc, Filter filter);

    EventJoinVO findCompletedEvent(long startId);
}
