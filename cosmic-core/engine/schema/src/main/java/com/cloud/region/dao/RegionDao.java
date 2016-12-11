package com.cloud.region.dao;

import com.cloud.region.RegionVO;
import com.cloud.utils.db.GenericDao;

public interface RegionDao extends GenericDao<RegionVO, Integer> {

    RegionVO findByName(String name);

    int getRegionId();
}
