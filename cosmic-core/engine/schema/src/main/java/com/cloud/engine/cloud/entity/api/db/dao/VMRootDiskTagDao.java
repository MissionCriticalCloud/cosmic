package com.cloud.engine.cloud.entity.api.db.dao;

import com.cloud.engine.cloud.entity.api.db.VMRootDiskTagVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VMRootDiskTagDao extends GenericDao<VMRootDiskTagVO, Long> {

    void persist(long vmId, List<String> diskTags);

    List<String> getRootDiskTags(long vmId);
}
