package org.apache.cloudstack.region;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PortableIpRangeDao extends GenericDao<PortableIpRangeVO, Long> {

    List<PortableIpRangeVO> listByRegionId(int regionId);
}
