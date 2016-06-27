package org.apache.cloudstack.region.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.region.RegionVO;

public interface RegionDao extends GenericDao<RegionVO, Integer> {

    RegionVO findByName(String name);

    int getRegionId();
}
