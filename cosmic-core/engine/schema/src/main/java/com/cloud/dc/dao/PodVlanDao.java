package com.cloud.dc.dao;

import com.cloud.dc.PodVlanVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PodVlanDao extends GenericDao<PodVlanVO, Long> {
    public List<PodVlanVO> listAllocatedVnets(long podId);

    public void add(long podId, int start, int end);

    public void delete(long podId);

    public PodVlanVO take(long podId, long accountId);

    public void release(String vlan, long podId, long accountId);
}
