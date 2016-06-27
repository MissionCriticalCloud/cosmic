package com.cloud.event.dao;

import com.cloud.event.UsageEventDetailsVO;
import com.cloud.utils.db.GenericDao;

import java.util.Map;

public interface UsageEventDetailsDao extends GenericDao<UsageEventDetailsVO, Long> {

    void persist(long eventId, Map<String, String> details);

    UsageEventDetailsVO findDetail(long eventId, String key);

    void deleteDetails(long eventId);
}
