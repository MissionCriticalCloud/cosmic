package com.cloud.event.dao;

import com.cloud.event.EventVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.SearchCriteria;

import java.util.Date;
import java.util.List;

public interface EventDao extends GenericDao<EventVO, Long> {
    public List<EventVO> searchAllEvents(SearchCriteria<EventVO> sc, Filter filter);

    public List<EventVO> listOlderEvents(Date oldTime);

    EventVO findCompletedEvent(long startId);

    public List<EventVO> listToArchiveOrDeleteEvents(List<Long> ids, String type, Date startDate, Date endDate, List<Long> accountIds);

    public void archiveEvents(List<EventVO> events);
}
