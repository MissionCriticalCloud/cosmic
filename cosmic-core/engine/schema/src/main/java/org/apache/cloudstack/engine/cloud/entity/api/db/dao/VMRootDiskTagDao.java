package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMRootDiskTagVO;

import java.util.List;

public interface VMRootDiskTagDao extends GenericDao<VMRootDiskTagVO, Long> {

    void persist(long vmId, List<String> diskTags);

    List<String> getRootDiskTags(long vmId);
}
