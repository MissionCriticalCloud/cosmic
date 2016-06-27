package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.host.HostTagVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface HostTagsDao extends GenericDao<HostTagVO, Long> {

    void persist(long hostId, List<String> hostTags);

    List<String> gethostTags(long hostId);
}
