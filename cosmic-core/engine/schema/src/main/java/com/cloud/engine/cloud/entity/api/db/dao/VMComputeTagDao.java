package com.cloud.engine.cloud.entity.api.db.dao;

import com.cloud.engine.cloud.entity.api.db.VMComputeTagVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VMComputeTagDao extends GenericDao<VMComputeTagVO, Long> {

    void persist(long vmId, List<String> computeTags);

    List<String> getComputeTags(long vmId);
}
