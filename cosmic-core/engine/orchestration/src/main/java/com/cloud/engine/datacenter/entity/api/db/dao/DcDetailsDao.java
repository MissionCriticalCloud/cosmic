package com.cloud.engine.datacenter.entity.api.db.dao;

import com.cloud.engine.datacenter.entity.api.db.DcDetailVO;
import com.cloud.utils.db.GenericDao;

import java.util.Map;

public interface DcDetailsDao extends GenericDao<DcDetailVO, Long> {
    Map<String, String> findDetails(long dcId);

    void persist(long dcId, Map<String, String> details);

    DcDetailVO findDetail(long dcId, String name);

    void deleteDetails(long dcId);
}
