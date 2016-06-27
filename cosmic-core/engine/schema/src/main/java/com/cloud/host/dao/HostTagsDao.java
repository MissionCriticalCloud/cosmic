package com.cloud.host.dao;

import com.cloud.host.HostTagVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface HostTagsDao extends GenericDao<HostTagVO, Long> {

    void persist(long hostId, List<String> hostTags);

    List<String> gethostTags(long hostId);

    List<String> getDistinctImplicitHostTags(List<Long> hostIds, String[] implicitHostTags);

    void deleteTags(long hostId);
}
