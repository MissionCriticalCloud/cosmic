package com.cloud.dc.dao;

import com.cloud.dc.HostPodVO;
import com.cloud.utils.db.GenericDao;

import java.util.HashMap;
import java.util.List;

public interface HostPodDao extends GenericDao<HostPodVO, Long> {
    public List<HostPodVO> listByDataCenterId(long id);

    public HostPodVO findByName(String name, long dcId);

    public HashMap<Long, List<Object>> getCurrentPodCidrSubnets(long zoneId, long podIdToSkip);

    public List<Long> listDisabledPods(long zoneId);

    public List<Long> listAllPods(long zoneId);
}
