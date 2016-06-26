package com.cloud.host.dao;

import com.cloud.host.DetailVO;
import com.cloud.utils.db.GenericDao;

import java.util.Map;

public interface HostDetailsDao extends GenericDao<DetailVO, Long> {
    Map<String, String> findDetails(long hostId);

    void persist(long hostId, Map<String, String> details);

    DetailVO findDetail(long hostId, String name);

    void deleteDetails(long hostId);
}
