package com.cloud.event.dao;

import com.cloud.event.UsageEventVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UsageEventDao extends GenericDao<UsageEventVO, Long> {

    public List<UsageEventVO> listLatestEvents(Date endDate);

    public List<UsageEventVO> getLatestEvent();

    List<UsageEventVO> getRecentEvents(Date endDate);

    List<UsageEventVO> listDirectIpEvents(Date startDate, Date endDate, long zoneId);

    void saveDetails(long eventId, Map<String, String> details);
}
